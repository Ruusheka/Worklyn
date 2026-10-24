import React from 'react';
import { CheckCircle2, AlertCircle, RefreshCw, Undo2, Clock, ArrowRight } from 'lucide-react';

/**
 * DAG Visualization View for HR-REWIND Multi-Step Workflows
 * Status codes:
 * - Green: COMPLETED
 * - Red: FAILED
 * - Blue: REPLAY ELIGIBLE
 * - Yellow: COMPENSATE ELIGIBLE
 * - Gray: PENDING / BLOCKED
 */
export function DependencyGraphView({ steps = [], failedStepId, onSelectStep, selectedStepId }) {
  if (!steps || steps.length === 0) {
    return (
      <div className="p-8 text-center text-[#89766C]">
        No DAG execution steps registered for this workflow.
      </div>
    );
  }

  const getStepStatus = (step) => {
    if (step.id === failedStepId || step.status === 'FAILED') return 'FAILED';
    if (step.status === 'COMPLETED') return 'COMPLETED';
    if (step.status === 'REPLAY_ELIGIBLE') return 'REPLAY';
    if (step.status === 'COMPENSATE_ELIGIBLE') return 'COMPENSATE';
    return 'PENDING';
  };

  const getStatusBadge = (statusType) => {
    switch (statusType) {
      case 'COMPLETED':
        return {
          bg: 'bg-[#EBF4EF]',
          border: 'border-[#387A58]',
          text: 'text-[#387A58]',
          icon: CheckCircle2,
          label: 'Completed',
        };
      case 'FAILED':
        return {
          bg: 'bg-[#FBECEB]',
          border: 'border-[#B85C55]',
          text: 'text-[#B85C55]',
          icon: AlertCircle,
          label: 'Failed',
        };
      case 'REPLAY':
        return {
          bg: 'bg-[#EEF4F8]',
          border: 'border-[#4B6B82]',
          text: 'text-[#4B6B82]',
          icon: RefreshCw,
          label: 'Replay Eligible',
        };
      case 'COMPENSATE':
        return {
          bg: 'bg-[#FDF5E8]',
          border: 'border-[#D69A43]',
          text: 'text-[#D69A43]',
          icon: Undo2,
          label: 'Compensate',
        };
      default:
        return {
          bg: 'bg-[#F3E6D6]/40',
          border: 'border-[#EAD8C3]',
          text: 'text-[#89766C]',
          icon: Clock,
          label: 'Pending',
        };
    }
  };

  return (
    <div className="p-5 bg-white/70 rounded-xl border border-[#EAD8C3]">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h4 className="text-sm font-bold text-[#342522]">Execution Dependency Graph (DAG)</h4>
          <p className="text-xs text-[#89766C]">
            Interactive topological step flow with compensating action tags
          </p>
        </div>
        {/* Legend */}
        <div className="flex flex-wrap gap-2 text-[11px]">
          <span className="flex items-center gap-1 text-[#387A58] bg-[#EBF4EF] px-2 py-0.5 rounded-full border border-[#387A58]/30">
            <span className="w-2 h-2 rounded-full bg-[#387A58]"></span> Completed
          </span>
          <span className="flex items-center gap-1 text-[#B85C55] bg-[#FBECEB] px-2 py-0.5 rounded-full border border-[#B85C55]/30">
            <span className="w-2 h-2 rounded-full bg-[#B85C55]"></span> Failed
          </span>
          <span className="flex items-center gap-1 text-[#4B6B82] bg-[#EEF4F8] px-2 py-0.5 rounded-full border border-[#4B6B82]/30">
            <span className="w-2 h-2 rounded-full bg-[#4B6B82]"></span> Replay
          </span>
          <span className="flex items-center gap-1 text-[#D69A43] bg-[#FDF5E8] px-2 py-0.5 rounded-full border border-[#D69A43]/30">
            <span className="w-2 h-2 rounded-full bg-[#D69A43]"></span> Compensate
          </span>
        </div>
      </div>

      {/* Nodes visual rendering */}
      <div className="flex flex-col lg:flex-row items-center justify-start gap-3 overflow-x-auto py-4 px-2">
        {steps.map((step, idx) => {
          const statusType = getStepStatus(step);
          const meta = getStatusBadge(statusType);
          const Icon = meta.icon;
          const isSelected = selectedStepId === step.id;

          return (
            <React.Fragment key={step.id}>
              {/* Step Node */}
              <div
                onClick={() => onSelectStep && onSelectStep(step)}
                className={`relative min-w-[210px] p-3.5 rounded-xl border-2 transition-all cursor-pointer shadow-xs ${
                  meta.bg
                } ${meta.border} ${isSelected ? 'ring-2 ring-[#541923] ring-offset-2 scale-102' : 'hover:scale-101'}`}
              >
                <div className="flex items-start justify-between mb-2">
                  <span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-white/80 border border-[#EAD8C3] text-[#342522]">
                    Step {idx + 1}
                  </span>
                  <div className={`flex items-center gap-1 text-[11px] font-semibold ${meta.text}`}>
                    <Icon className="w-3.5 h-3.5" />
                    <span>{meta.label}</span>
                  </div>
                </div>

                <p className="text-xs font-bold text-[#342522] leading-snug line-clamp-2">
                  {step.name || step.id}
                </p>

                <div className="mt-2.5 pt-2 border-t border-[#EAD8C3]/50 flex items-center justify-between text-[11px] text-[#89766C]">
                  <span>ID: <code className="text-[10px] font-mono text-[#541923]">{step.id}</code></span>
                  {step.durationMs !== undefined && (
                    <span>{step.durationMs}ms</span>
                  )}
                </div>

                {step.dependsOn && step.dependsOn.length > 0 && (
                  <p className="text-[10px] text-[#89766C] mt-1 truncate">
                    Deps: {step.dependsOn.join(', ')}
                  </p>
                )}
              </div>

              {/* Dependency Connector Arrow */}
              {idx < steps.length - 1 && (
                <div className="shrink-0 text-[#89766C]/60 flex items-center justify-center my-1 lg:my-0">
                  <ArrowRight className="w-5 h-5 hidden lg:block" />
                  <div className="w-0.5 h-4 bg-[#EAD8C3] lg:hidden"></div>
                </div>
              )}
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );
}

export default DependencyGraphView;
