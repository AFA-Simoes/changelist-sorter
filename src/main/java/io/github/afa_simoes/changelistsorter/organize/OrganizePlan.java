package io.github.afa_simoes.changelistsorter.organize;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of planning an organize run: every file that should move, and where to.
 * Building this list is kept separate from applying it, so planning stays a pure,
 * platform-mutation-free operation that can be inspected (and unit-tested) before anything
 * actually moves.
 */
public class OrganizePlan {
    private final List<Move> moves = new ArrayList<>();

    public void addMove(VirtualFile file, String targetChangeListName, boolean requiresConfirmation) {
        moves.add(new Move(file, targetChangeListName, requiresConfirmation));
    }

    public List<Move> getMoves() {
        return moves;
    }

    public boolean isEmpty() {
        return moves.isEmpty();
    }

    public boolean anyRequiresConfirmation() {
        return moves.stream().anyMatch(Move::requiresConfirmation);
    }

    public record Move(VirtualFile file, String targetChangeListName, boolean requiresConfirmation) {
    }
}
