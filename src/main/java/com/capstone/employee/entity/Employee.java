package com.capstone.employee.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Employee {

	private int EmployeeID;
	private String EmployeeName;
	private String DateOfBirth;
}
