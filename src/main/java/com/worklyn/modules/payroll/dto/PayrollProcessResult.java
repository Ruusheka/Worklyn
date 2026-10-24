package com.worklyn.modules.payroll.dto;
import java.util.List;
public record PayrollProcessResult(String period,int processedCount,List<PayrollResponse> records,boolean replayed) {}
