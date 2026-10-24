import React, { useState } from 'react';
import { AlertOctagon, CheckCircle2, ShieldAlert } from 'lucide-react';
import Modal from '../common/Modal.jsx';
import Button from '../common/Button.jsx';

export function RecoveryExecutionModal({
  isOpen,
  onClose,
  workflow,
  plan,
  onExecuteRecovery,
  isExecuting = false,
}) {
  const [confirmInput, setConfirmInput] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  if (!workflow) return null;

  const compensations = plan?.actions?.filter((a) => a.action === 'COMPENSATE') || [];
  const replays = plan?.actions?.filter((a) => a.action === 'REPLAY') || [];

  const handleConfirm = async () => {
    if (confirmInput.trim().toUpperCase() !== 'CONFIRM RECOVER') {
      setErrorMsg('Please type "CONFIRM RECOVER" exactly to proceed.');
      return;
    }
    setErrorMsg('');
    try {
      await onExecuteRecovery(workflow.workflowId);
      setConfirmInput('');
      onClose();
    } catch (err) {
      setErrorMsg(err.message || 'Recovery execution failed.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => {
        setConfirmInput('');
        setErrorMsg('');
        onClose();
      }}
      title="Authorize Workflow Recovery"
      subtitle={`Target Workflow: ${workflow.workflowId}`}
      maxWidth="max-w-lg"
      footer={
        <>
          <Button
            variant="secondary"
            onClick={() => {
              setConfirmInput('');
              setErrorMsg('');
              onClose();
            }}
            disabled={isExecuting}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={handleConfirm}
            isLoading={isExecuting}
            disabled={confirmInput.trim().toUpperCase() !== 'CONFIRM RECOVER' || isExecuting}
          >
            Execute Recovery
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        {/* Warning Callout */}
        <div className="p-4 rounded-xl bg-[#FBECEB] border border-[#B85C55]/30 flex items-start gap-3">
          <ShieldAlert className="w-5 h-5 text-[#B85C55] shrink-0 mt-0.5" />
          <div className="text-xs text-[#342522]">
            <p className="font-bold text-[#B85C55] mb-1">State Mutation & Compensating Actions Warning</p>
            <p className="text-[#89766C]">
              Executing recovery will trigger atomic compensating transactions and replay failed DAG steps with an idempotency lock. This operation alters database state.
            </p>
          </div>
        </div>

        {/* Action Summary */}
        <div className="p-3.5 bg-white rounded-xl border border-[#EAD8C3] text-xs space-y-2">
          <div className="flex justify-between items-center text-[#342522]">
            <span className="font-semibold">Replay Operations:</span>
            <span className="font-mono font-bold text-[#4B6B82] bg-[#EEF4F8] px-2 py-0.5 rounded">
              {replays.length} steps
            </span>
          </div>
          <div className="flex justify-between items-center text-[#342522]">
            <span className="font-semibold">Compensating Rollbacks:</span>
            <span className="font-mono font-bold text-[#D69A43] bg-[#FDF5E8] px-2 py-0.5 rounded">
              {compensations.length} steps
            </span>
          </div>
          <div className="flex justify-between items-center text-[#342522]">
            <span className="font-semibold">Invariant Safety Verification:</span>
            <span className="font-semibold text-[#387A58] flex items-center gap-1">
              <CheckCircle2 className="w-3.5 h-3.5" /> Passed
            </span>
          </div>
        </div>

        {/* Typed Confirmation Input */}
        <div className="space-y-1.5">
          <label className="block text-xs font-bold text-[#342522]">
            To proceed, type <span className="font-mono text-[#B85C55] font-extrabold select-all">CONFIRM RECOVER</span> below:
          </label>
          <input
            type="text"
            value={confirmInput}
            onChange={(e) => {
              setConfirmInput(e.target.value);
              if (errorMsg) setErrorMsg('');
            }}
            placeholder="CONFIRM RECOVER"
            className="w-full px-3.5 py-2.5 rounded-lg border border-[#EAD8C3] bg-white text-sm font-mono focus:outline-none focus:border-[#B85C55] focus:ring-1 focus:ring-[#B85C55]"
          />
          {errorMsg && (
            <p className="text-xs text-[#B85C55] font-medium flex items-center gap-1">
              <AlertOctagon className="w-3.5 h-3.5" /> {errorMsg}
            </p>
          )}
        </div>
      </div>
    </Modal>
  );
}

export default RecoveryExecutionModal;
