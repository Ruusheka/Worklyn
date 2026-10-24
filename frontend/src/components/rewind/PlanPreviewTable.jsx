import React from 'react';
import { ShieldCheck, AlertTriangle, ArrowRightCircle } from 'lucide-react';

export function PlanPreviewTable({ plan, isLoading = false }) {
  if (isLoading) {
    return (
      <div className="p-8 text-center text-[#89766C] bg-white rounded-xl border border-[#EAD8C3]">
        <div className="inline-block animate-spin h-6 w-6 border-2 border-[#541923] border-t-transparent rounded-full mb-2"></div>
        <p className="text-xs">Computing non-mutating recovery plan...</p>
      </div>
    );
  }

  if (!plan) {
    return (
      <div className="p-6 text-center text-[#89766C] bg-white rounded-xl border border-[#EAD8C3]">
        Select a workflow to preview computed recovery actions.
      </div>
    );
  }

  const { workflowId, workflowType, failedStepId, failureReason, actions = [], invariantsSatisfied } = plan;

  return (
    <div className="bg-white rounded-xl border border-[#EAD8C3] overflow-hidden shadow-soft">
      {/* Plan Header */}
      <div className="p-4 bg-[#F8EFE4] border-b border-[#EAD8C3] flex flex-wrap items-center justify-between gap-3">
        <div>
          <span className="text-[10px] uppercase font-bold text-[#541923] tracking-wider block">
            Generated Plan Preview (Non-Mutating)
          </span>
          <h4 className="text-sm font-bold text-[#342522]">
            Workflow: <span className="font-mono text-[#541923]">{workflowId}</span> ({workflowType})
          </h4>
        </div>

        {/* Invariant status badge */}
        <div className="flex items-center gap-2">
          {invariantsSatisfied ? (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-[#EBF4EF] text-[#387A58] border border-[#387A58]/30">
              <ShieldCheck className="w-4 h-4" />
              Invariants Verified
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-[#FBECEB] text-[#B85C55] border border-[#B85C55]/30">
              <AlertTriangle className="w-4 h-4" />
              Invariant Violation Risk
            </span>
          )}
        </div>
      </div>

      {/* Failure Diagnostic Alert */}
      {failedStepId && (
        <div className="p-3.5 bg-[#FBECEB]/70 border-b border-[#B85C55]/20 flex items-start gap-3">
          <AlertTriangle className="w-4 h-4 text-[#B85C55] shrink-0 mt-0.5" />
          <div className="text-xs">
            <span className="font-bold text-[#B85C55]">Root Failure in Step:</span>{' '}
            <code className="bg-white/80 px-1 py-0.5 rounded font-mono font-bold text-[#541923]">{failedStepId}</code>
            <p className="text-[#89766C] mt-0.5">{failureReason}</p>
          </div>
        </div>
      )}

      {/* Planned Actions Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-[#342522]">
          <thead className="bg-[#F3E6D6]/60 text-[11px] uppercase font-semibold text-[#89766C] border-b border-[#EAD8C3]">
            <tr>
              <th className="px-4 py-3">Step ID</th>
              <th className="px-4 py-3">Proposed Action</th>
              <th className="px-4 py-3">Engine Reasoning</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#EAD8C3]/70">
            {actions.map((act, idx) => {
              let badgeColor = 'bg-[#EEF4F8] text-[#4B6B82] border-[#4B6B82]/30';
              if (act.action === 'REPLAY') badgeColor = 'bg-[#EEF4F8] text-[#4B6B82] border-[#4B6B82]/40 font-bold';
              if (act.action === 'COMPENSATE') badgeColor = 'bg-[#FDF5E8] text-[#D69A43] border-[#D69A43]/40 font-bold';
              if (act.action === 'PRESERVE') badgeColor = 'bg-[#EBF4EF] text-[#387A58] border-[#387A58]/40';

              return (
                <tr key={act.stepId || idx} className="hover:bg-[#FBF5EC]">
                  <td className="px-4 py-3 font-mono font-bold text-[#541923] whitespace-nowrap">
                    {act.stepId}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">
                    <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] border ${badgeColor}`}>
                      <ArrowRightCircle className="w-3 h-3" />
                      {act.action}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-[#342522]">
                    {act.reason}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default PlanPreviewTable;
