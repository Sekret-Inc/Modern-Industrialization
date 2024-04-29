/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.base;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A base participant implementation that modifies itself during transactions,
 * saving snapshots of its state in objects of type {@code T} in case it needs to revert to a previous state.
 *
 * <h3>How to use from subclasses</h3>
 * <ul>
 * <li>Call {@link #updateSnapshots} right before the state of your subclass is modified in a transaction.</li>
 * <li>Override {@link #createSnapshot}: it is called when necessary to create an object representing the state of your subclass.</li>
 * <li>Override {@link #readSnapshot}: it is called when necessary to revert to a previous state of your subclass.</li>
 * <li>You may optionally override {@link #onFinalCommit}: it is called at the of a transaction that modified the state.
 * For example, it could contain a call to {@code markDirty()}.</li>
 * <li>(Advanced!) You may optionally override {@link #releaseSnapshot}: it is called once a snapshot object will not be used,
 * for example you may wish to pool expensive state objects.</li>
 * </ul>
 *
 * <h3>More technical explanation</h3>
 *
 * <p>
 * {@link #updateSnapshots} should be called before any modification.
 * This will save the state of this participant using {@link #createSnapshot} if no state was already saved for that transaction.
 * When the transaction is aborted and changes need to be rolled back, {@link #readSnapshot} will be called
 * to signal that the current state should revert to that of the snapshot.
 * The snapshot object is then {@linkplain #releaseSnapshot released}, and can be cached for subsequent use, or discarded.
 *
 * <p>
 * When an outer transaction is committed, {@link #readSnapshot} will not be called so that the current state of this participant
 * is retained. {@link #releaseSnapshot} will be called because the snapshot is not necessary anymore,
 * and {@link #onFinalCommit} will be called after the transaction is closed.
 *
 * @param <T> The objects that this participant uses to save its state snapshots.
 */
public abstract class SnapshotParticipant<T> implements Transaction.CloseCallback, Transaction.OuterCloseCallback {
    private final List<T> snapshots = new ArrayList<>();

    /**
     * Return a new <b>nonnull</b> object containing the current state of this participant.
     * <b>{@code null} may not be returned, or an exception will be thrown!</b>
     */
    protected abstract T createSnapshot();

    /**
     * Roll back to a state previously created by {@link #createSnapshot}.
     */
    protected abstract void readSnapshot(T snapshot);

    /**
     * Signals that the snapshot will not be used anymore, and is safe to cache for next calls to {@link #createSnapshot},
     * or discard entirely.
     */
    protected void releaseSnapshot(T snapshot) {
    }

    /**
     * Called after an outer transaction succeeded,
     * to perform irreversible actions such as {@code markDirty()} or neighbor updates.
     */
    protected void onFinalCommit() {
    }

    /**
     * Update the stored snapshots so that the changes happening as part of the passed transaction can be correctly
     * committed or rolled back.
     * This function should be called every time the participant is about to change its internal state as part of a transaction.
     */
    public void updateSnapshots(TransactionContext transaction) {
        // Make sure we have enough storage for snapshots
        while (snapshots.size() <= transaction.nestingDepth()) {
            snapshots.add(null);
        }

        // If the snapshot is null, we need to create it, and we need to register a callback.
        if (snapshots.get(transaction.nestingDepth()) == null) {
            T snapshot = createSnapshot();
            Objects.requireNonNull(snapshot, "Snapshot may not be null!");

            snapshots.set(transaction.nestingDepth(), snapshot);
            transaction.addCloseCallback(this);
        }
    }

    @Override
    public void onClose(TransactionContext transaction, Transaction.Result result) {
        // Get and remove the relevant snapshot.
        T snapshot = snapshots.set(transaction.nestingDepth(), null);

        if (result.wasAborted()) {
            // If the transaction was aborted, we just revert to the state of the snapshot.
            readSnapshot(snapshot);
            releaseSnapshot(snapshot);
        } else if (transaction.nestingDepth() > 0) {
            if (snapshots.get(transaction.nestingDepth() - 1) == null) {
                // No snapshot yet, so move the snapshot one nesting level up.
                snapshots.set(transaction.nestingDepth() - 1, snapshot);
                // This is the first snapshot at this level: we need to call addCloseCallback.
                transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
            } else {
                // There is already an older snapshot at the nesting level above, just release the newer one.
                releaseSnapshot(snapshot);
            }
        } else {
            releaseSnapshot(snapshot);
            transaction.addOuterCloseCallback(this);
        }
    }

    @Override
    public void afterOuterClose(Transaction.Result result) {
        // The result is guaranteed to be COMMITTED,
        // as this is only scheduled during onClose() when the outer transaction is successful.
        onFinalCommit();
    }
}
