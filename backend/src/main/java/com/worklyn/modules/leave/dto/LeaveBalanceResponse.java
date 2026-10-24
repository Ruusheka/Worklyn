package com.worklyn.modules.leave.dto;
import com.worklyn.modules.leave.model.LeaveBalance;
public record LeaveBalanceResponse(String employeeId,int year,int casualAvailable,int sickAvailable,int earnedAvailable,int casualUsed,int sickUsed,int earnedUsed){public static LeaveBalanceResponse from(LeaveBalance b){return new LeaveBalanceResponse(b.getEmployeeId(),b.getYear(),b.getCasualLeaveAvailable(),b.getSickLeaveAvailable(),b.getEarnedLeaveAvailable(),b.getCasualLeaveUsed(),b.getSickLeaveUsed(),b.getEarnedLeaveUsed());}}
