// DiagnosticsProviderService.aidl
package com.poynt.diagnostics;

import com.poynt.diagnostics.IDiagnosticsServiceRunner;

// Declare any non-default types here with import statements

interface IDiagnosticsProviderService {
    List<String> getDiagnosticStepIds();
    String getDiagnosticStepName(String stepId);
    boolean setCallback(IDiagnosticsServiceRunner runner);
    boolean startStep(String stepId);
    boolean cancelStep(String stepId);
    boolean successDependsOnUser(String stepId);
}
