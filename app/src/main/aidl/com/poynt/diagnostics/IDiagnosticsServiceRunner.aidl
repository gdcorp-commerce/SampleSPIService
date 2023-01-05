// DiagnosticsProviderListener.aidl
package com.poynt.diagnostics;

// Declare any non-default types here with import statements

interface IDiagnosticsServiceRunner {
    void onDiagnosticStepStarted(String stepId, String details);
    void onDiagnosticStepSuccess(String stepId, String details);
    void onDiagnosticStepWarning(String stepId, String details);
    void onDiagnosticStepFailed(String stepId, String details);
    void onDiagnosticsStepDetailsUpdated(String stepId, String details);
}
