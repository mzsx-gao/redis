package com.gao.redis.springdata;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Member implements Serializable {

	private String mid ;
	private String name ;
	private Integer age ;
	private Date birthday ;
	private Double salary ;
	
	@Override
	public String toString() {
		return "Member [mid=" + mid + ", name=" + name + ", age=" + age
				+ ", birthday=" + birthday + ", salary=" + salary + "]";
	}
	
}
