import React from 'react';

export function StatusBadge({ status, className = '' }) {
  if (!status) return null;

  const rawStatus = String(status).toUpperCase();

  let styles = 'bg-[#EBF4EF] text-[#387A58] border-[#387A58]/20';

  if (['PRESENT', 'ACTIVE', 'APPROVED', 'COMPLETED', 'ON_TRACK', 'OPEN', 'SHORTLISTED', 'OFFER_EXTENDED'].includes(rawStatus)) {
    styles = 'bg-[#EBF4EF] text-[#387A58] border-[#387A58]/30';
  } else if (['PENDING', 'WARNING', 'LATE', 'ATTENTION_NEEDED', 'REVIEWING', 'INTERVIEWING', 'PROCESSING'].includes(rawStatus)) {
    styles = 'bg-[#FDF5E8] text-[#D69A43] border-[#D69A43]/30';
  } else if (['REJECTED', 'ABSENT', 'FAILED', 'INACTIVE', 'TERMINATED', 'CANCELLED', 'REQUIRES_MANUAL_REVIEW'].includes(rawStatus)) {
    styles = 'bg-[#FBECEB] text-[#B85C55] border-[#B85C55]/30';
  } else if (['HALF_DAY', 'ON_LEAVE', 'PROBATION', 'DRAFT', 'INFO', 'FINALIZED'].includes(rawStatus)) {
    styles = 'bg-[#EEF4F8] text-[#4B6B82] border-[#4B6B82]/30';
  }

  const label = rawStatus.replace(/_/g, ' ');

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${styles} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current mr-1.5 opacity-80"></span>
      {label}
    </span>
  );
}

export default StatusBadge;
