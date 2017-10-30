package com.gao.redis.springdata;

import java.io.Serializable;
import java.util.Date;

public class Member implements Serializable {
	private String mid ;
	private String name ;
	private Integer age ;
	private Date birthday ;
	private Double salary ;
	
	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Double getSalary() {
		return salary;
	}

	public void setSalary(Double salary) {
		this.salary = salary;
	}

	@Override
	public String toString() {
		return "Member [mid=" + mid + ", name=" + name + ", age=" + age
				+ ", birthday=" + birthday + ", salary=" + salary + "]";
	}
	
}
