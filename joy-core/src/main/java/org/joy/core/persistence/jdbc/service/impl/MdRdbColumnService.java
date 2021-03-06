package org.joy.core.persistence.jdbc.service.impl;

import org.joy.commons.log.Log;
import org.joy.commons.log.LogFactory;
import org.joy.core.persistence.jdbc.model.vo.MdRdbColumn;
import org.joy.core.persistence.jdbc.model.vo.MdRdbColumnComment;
import org.joy.core.persistence.jdbc.model.vo.MdRdbTable;
import org.joy.core.persistence.jdbc.model.vo.RdbConnection;
import org.joy.core.persistence.jdbc.service.IMdRdbColumnService;
import org.joy.core.persistence.jdbc.support.MdRdbColumnCommentParser;
import org.joy.core.persistence.jdbc.support.db.DbSupport;
import org.joy.core.persistence.jdbc.support.db.DbSupportFactory;
import org.joy.core.persistence.jdbc.support.utils.MdRdbTool;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 关系型数据库列元数据信息服务
 *
 * @since 1.0.0
 * @author Kevice
 * @time 2013-1-2 下午10:38:44
 */
public class MdRdbColumnService implements IMdRdbColumnService {

	protected static final Log logger = LogFactory.getLog(MdRdbColumnService.class);

	@Override
	public Map<String, MdRdbColumn> getColumns(RdbConnection connection, String tableName) {
		logger.info("加载表字段元数据信息，datasourceId: " + connection.getDsId() + ", table: " + tableName);
		Map<String, MdRdbColumn> columnMap = null;
		Connection conn = connection.getConnection();

		try {
            DatabaseMetaData metaData = conn.getMetaData();
            DbSupport dbSupport = DbSupportFactory.createDbSupport(conn);
            columnMap = loadColumns(dbSupport, metaData, tableName);

			// 设置主键标识
			List<String> pks = MdRdbPrimaryKeyService.getPrimaryKey(dbSupport, metaData, tableName);
			for (String pk : pks) {
				MdRdbColumn column = columnMap.get(pk);
				column.setKey(true);
			}
			
			// 与表关联
			MdRdbTable table = MdRdbTool.getRelationalObject(connection, tableName);
			table.setColumns(columnMap.values());
		} catch (Exception e) {
			logger.error(e);
		}
		return columnMap;
	}

	private Map<String, MdRdbColumn> loadColumns(DbSupport dbSupport, DatabaseMetaData metaData, String tableName)
			throws SQLException {
		Map<String, MdRdbColumn> columnMap = new LinkedCaseInsensitiveMap<MdRdbColumn>() {

			@Override
			public Collection<MdRdbColumn> values() {
				return new ArrayList<MdRdbColumn>(super.values()); // 父类默认返回的集合不是可序列化的
			}
		};

        String schema = dbSupport.getCurrentSchema().getName();
        String[] tables = {tableName.toLowerCase(), tableName.toUpperCase(), tableName};
        for (String t : tables) {
            ResultSet rs = metaData.getColumns(null, schema, t, null);
            while (rs.next()) {
                MdRdbColumn column = createColumn(rs);
                columnMap.put(column.getName(), column);
            }
            if(!columnMap.isEmpty()) {
                break;
            }
        }
		return columnMap;
	}

	/**
	 * 创建数据字段对象
	 * 
	 * @param columns 字段结果集
	 * @return 数据字段对象
	 * @throws SQLException
	 * @author Kevice
	 * @time 2012-11-9 上午10:15:07
	 */
	private static MdRdbColumn createColumn(ResultSet columns) throws SQLException {
		MdRdbColumn column = new MdRdbColumn();
		column.setType(columns.getString("TYPE_NAME"));
		column.setName(columns.getString("COLUMN_NAME"));
		String columnDef = columns.getString("COLUMN_DEF");
		if (columnDef != null) {
			columnDef = columnDef.trim();
			if (columnDef.equals("b'1'")) {
				columnDef = "true";
			} else if (columnDef.equals("b'0'")) {
				columnDef = "false";
			}
		}
		column.setDefaultValue(columnDef);
		String remarks = columns.getString("REMARKS");
		MdRdbColumnComment comment = MdRdbColumnCommentParser.parse(remarks);
		column.setComment(comment);
		int nullable = columns.getInt("NULLABLE");
		switch (nullable) {
		case DatabaseMetaData.columnNoNulls:
			column.setNullable(false);
			break;
		case DatabaseMetaData.columnNullable:
			column.setNullable(true);
			break;
		case DatabaseMetaData.columnNullableUnknown:
			column.setNullable(null);
			break;
		}
		column.setLength(columns.getInt("COLUMN_SIZE"));
		column.setPrecision(new BigDecimal(columns.getInt("DECIMAL_DIGITS")));
		return column;
	}

}
