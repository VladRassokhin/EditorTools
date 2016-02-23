package ksp.kos.ideaplugin.reference;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import ksp.kos.ideaplugin.KerboScriptFile;
import ksp.kos.ideaplugin.psi.*;

import java.util.ArrayList;
import java.util.function.BiFunction;

/**
 * Created on 08/01/16.
 *
 * @author ptasha
 */
public class FileScope extends LocalScope {
    private KerboScriptFile kerboScriptFile;
    private final ArrayList<KerboScriptFile> dependencies = new ArrayList<>();
    private final ScopeMap virtualFunctions = new ScopeMap();
    private final ScopeMap virtualVariables = new ScopeMap();

    public FileScope(KerboScriptFile kerboScriptFile) {
        this.kerboScriptFile = kerboScriptFile;
    }

    public void addDependency(KerboScriptNamedElement element) {
        KerboScriptFile dependency = kerboScriptFile.resolveFile(element);
        if (dependency != null && !dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
    }

    public ArrayList<KerboScriptFile> getDependencies() {
        return dependencies;
    }

    @Override
    public void clear() {
        dependencies.clear();
        virtualFunctions.clear();
        virtualVariables.clear();
        super.clear();
    }

    public KerboScriptNamedElement getVirtualFunction(KerboScriptNamedElement element) {
        KerboScriptNamedElement function = virtualFunctions.get(element.getName());
        if (function == null) {
            function = createVirtualFunction(element.getName());
            virtualFunctions.put(element.getName(), function);
        }
        return function;
    }

    @SuppressWarnings("ConstantConditions")
    private KerboScriptDeclareFunctionClause createVirtualFunction(String name) {
        return KerboScriptElementFactory.function(kerboScriptFile.getProject(), name);
    }

    public KerboScriptNamedElement getVirtualVariable(KerboScriptNamedElement element) {
        KerboScriptNamedElement variable = virtualVariables.get(element.getName());
        if (variable == null) {
            variable = createVirtualVariable(element.getName());
            virtualVariables.put(element.getName(), variable);
        }
        return variable;
    }

    @SuppressWarnings("ConstantConditions")
    private KerboScriptNamedElement createVirtualVariable(String name) {
        Project project = kerboScriptFile.getProject();
        return KerboScriptElementFactory.variable(project, name);
    }

    public enum Resolver {
        FUNCTION(FileScope::resolveFunction, FileScope::getVirtualFunction),
        VARIABLE(FileScope::resolveVariable, FileScope::getVirtualVariable);

        private final BiFunction<FileScope, KerboScriptNamedElement, KerboScriptNamedElement> findRegistered;
        private final BiFunction<FileScope, KerboScriptNamedElement, KerboScriptNamedElement> createVirtual;

        Resolver(BiFunction<FileScope, KerboScriptNamedElement, KerboScriptNamedElement> findRegistered,
                 BiFunction<FileScope, KerboScriptNamedElement, KerboScriptNamedElement> createVirtual) {
            this.findRegistered = findRegistered;
            this.createVirtual = createVirtual;
        }

        public PsiElement resolve(KerboScriptFile file,
                                  KerboScriptNamedElement element) {
            PsiElement resolved = findRegistered.apply(file.getFileScope(), element);
            if (resolved == null) {
                for (KerboScriptFile dependency : file.getFileScope().getDependencies()) {
                    resolved = findRegistered.apply(dependency.getFileScope(), element);
                    if (resolved != null) {
                        return resolved;
                    }
                }
                return createVirtual.apply(file.getFileScope(), element);
            }
            return resolved;
        }
    }
}
