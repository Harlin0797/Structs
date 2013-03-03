package it.fb.structs.apt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Flavio
 */
public class Tarjan<N> {
    
    private final Iterable<N> nodes;
    private final Dependents<N> dependents;
    private final List<N> result = new ArrayList<N>();
    private final Set<N> permMarks = new HashSet<N>();
    private final Set<N> tempMarks = new LinkedHashSet<N>();

    private Tarjan(Iterable<N> nodes, Dependents<N> dependents) {
        this.nodes = nodes;
        this.dependents = dependents;
    }
    
    private void start() {
        for (N node : nodes) {
            if (!permMarks.contains(node)) {
                visit(node);
            }
        }
    }
    
    private void visit(N node) {
        if (tempMarks.contains(node)) {
            throw new IllegalStateException("Dependency cycle detected: " + tempMarks.toString());
        }
        if (!permMarks.contains(node)) {
            tempMarks.add(node);
            for (N depNode : dependents.getDependents(node)) {
                visit(depNode);
            }
            tempMarks.remove(node);
            permMarks.add(node);
            result.add(node);
        }
    }

    public static <N> List<N> topologicalSort(Iterable<N> nodes, Dependents<N> dependents) {
        Tarjan<N> tarjan = new Tarjan<N>(nodes, dependents);
        tarjan.start();
        return tarjan.result;
    }
    
    public interface Dependents<N> {
        public Iterable<N> getDependents(N node);
    }
}
