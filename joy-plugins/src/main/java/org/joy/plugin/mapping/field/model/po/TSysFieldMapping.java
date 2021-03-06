package org.joy.plugin.mapping.field.model.po;

import org.joy.commons.bean.IEntity;
import org.joy.core.persistence.orm.jpa.annotations.Comment;

import javax.persistence.*;

/**
 * 字段映射信息实体
 *
 * @since 1.0.0
 * @author Kevice
 * @time 2011-12-05 下午 2:00:02
 */
@Entity
@Table(name = "t_sys_field_mapping")
@Comment("字段映射")
public class TSysFieldMapping implements IEntity<String> {
	
	private String id; // 规则id，外键
	private String field1; // 对象1的字段
	private String field2; // 对象2的字段
	private String fieldType; // 字段类型
	private String remark;   // 字段描述
	private TSysFieldMappingRule rule; // 映射规则对象
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(length = 32, nullable = false)
	@Comment("主键")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Column(length = 64, nullable = false)
	@Comment("字段1")
	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	@Column(length = 64, nullable = false)
	@Comment("字段2")
	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	@Column(length = 128, nullable = false)
	@Comment("描述")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String desc) {
		this.remark = desc;
	}

	@Column(length = 2, nullable = false)
	@Comment("字段类型")
	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, optional = false)
	@JoinColumn(name = "RULE_ID") 
	@Comment("映射规则ID")
	public TSysFieldMappingRule getRule() {
		return rule;
	}

	public void setRule(TSysFieldMappingRule rule) {
		this.rule = rule;
	}
	
}
