package com.wy.model;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.wy.base.AbstractModel;
import com.wy.database.Pri;
import com.wy.database.Unique;
import com.wy.enums.RoleState;
import com.wy.excel.ExcelColumn;
import com.wy.valid.ValidEdit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 角色表 ts_role
 * 
 * @author 飞花梦影
 * @date 2021-01-20 17:30:19
 * @git {@link https://github.com/mygodness100}
 */
@ApiModel(description = "角色表 ts_role")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractModel {

	private static final long serialVersionUID = 1L;

	/**
	 * 角色编号
	 */
	@ApiModelProperty("角色编号")
	@ExcelColumn("角色序号")
	@NotNull(groups = ValidEdit.class)
	@Pri
	private Long roleId;

	/**
	 * 角色名
	 */
	@ApiModelProperty("角色名")
	@ExcelColumn("角色名称")
	@NotBlank(message = "角色名称不能为空")
	@Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
	private String roleName;

	/**
	 * 角色编码
	 */
	@ApiModelProperty("角色编码")
	@Unique
	private String roleCode;

	/**
	 * 角色类型:0不可见,只有超级管理员不可见;默认1可见
	 */
	@ApiModelProperty("角色类型:0不可见,只有超级管理员不可见;默认1可见")
	private Integer roleType;

	/**
	 * 角色状态:0停用;默认1正常;2逻辑删除
	 */
	@ApiModelProperty("角色状态:0停用;默认1正常;2逻辑删除")
	@ExcelColumn(value = "角色状态", propConverter = RoleState.class)
	private Integer roleState;

	/**
	 * 角色描述
	 */
	@ApiModelProperty("角色描述")
	private String roleDesc;

	/** 非数据库字段 */
	/**
	 * 原角色编号
	 */
	@ApiModelProperty("原角色编号")
	@NotBlank(groups = ValidEdit.class)
	private String oriRoleCode;

	/**
	 * 菜单集合
	 */
	@ApiModelProperty("菜单集合")
	private List<Menu> menus;

	/**
	 * 权限集合
	 */
	@ApiModelProperty("权限集合")
	private List<RoleMenu> roleMenus;

	/**
	 * 部门集合
	 */
	@ApiModelProperty("部门集合")
	private List<Depart> departs;
}