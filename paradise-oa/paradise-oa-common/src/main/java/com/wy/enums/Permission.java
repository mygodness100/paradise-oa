package com.wy.enums;

import com.wy.common.PropConverter;

/**
 * 权限枚举
 * 
 * @author ParadiseWY
 * @date 2020-11-25 00:00:11
 * @git {@link https://github.com/mygodness100}
 */
public enum Permission implements PropConverter {

	ALL("所有权限"),
	INSERT("新增"),
	DELETE("删除"),
	UPDATE("修改"),
	SELECT("查询"),
	IMPORT("导入"),
	EXPORT("导出"),
	CLEAR("清除数据");

	private String msg;

	private Permission(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return this.ordinal();
	}

	@Override
	public Object getValue() {
		return this.msg;
	}
}