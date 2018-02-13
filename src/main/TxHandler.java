package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is {@code utxoPool}. This should make a copy of
     * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if: (1) all outputs claimed by {@code tx} are in the current
     *         UTXO pool, (2) the signatures on each input of {@code tx} are
     *         valid, (3) no UTXO is claimed multiple times by {@code tx}, (4)
     *         all of {@code tx}s output values are non-negative, and (5) the
     *         sum of {@code tx}s input values is greater than or equal to the
     *         sum of its output values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        return outputsAreInUTXOPool(tx) && isValidSignature(tx) && noDuplicatedUXTO(tx) && outPutValuesNonNegative(tx)
                && inputValuesGTEOutput(tx);

    }

    /**
     * Handles each epoch by receiving an unordered array of proposed
     * transactions, checking each transaction for correctness, returning a
     * mutually valid array of accepted transactions, and updating the current
     * UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> validTxs = new ArrayList<>();

        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);
                removeTransactionOutputsFromPool(tx);
                addTransactionUXTOListToPool(tx);
            }
        }
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }

    private void addTransactionUXTOListToPool(Transaction tx) {
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output output = tx.getOutput(i);
            utxoPool.addUTXO(new UTXO(tx.getHash(), i), output);
        }
    }

    private void removeTransactionOutputsFromPool(Transaction tx) {
        for (Transaction.Input i : tx.getInputs()) {
            UTXO utxo = new UTXO(i.prevTxHash, i.outputIndex);
            utxoPool.removeUTXO(utxo);
        }
    }

    private boolean outputsAreInUTXOPool(Transaction tx) {

        for (Transaction.Input in : tx.getInputs()) {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!utxoPool.contains(utxo)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidSignature(Transaction tx) {
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) {
                return false;
            }
        }
        return true;
    }

    private boolean noDuplicatedUXTO(Transaction tx) {
        Set<UTXO> utxoSet = new HashSet<>();

        for (Transaction.Input i : tx.getInputs()) {
            UTXO utxo = new UTXO(i.prevTxHash, i.outputIndex);
            if (utxoSet.contains(utxo)) {
                return false;
            }
            utxoSet.add(utxo);
        }

        return true;

    }

    /*
     * private boolean outPutValuesNonNegative(Transaction tx) { for
     * (Transaction.Input i : tx.getInputs()) { UTXO utxo = new
     * UTXO(i.prevTxHash, i.outputIndex); Transaction.Output output =
     * utxoPool.getTxOutput(utxo); if (output.value < 0) { return false; } }
     * 
     * return true; }
     */

    private boolean outPutValuesNonNegative(Transaction tx) {
        for (Transaction.Output output : tx.getOutputs()) {

            if (output.value < 0) {
                return false;
            }
        }

        return true;
    }

    private boolean inputValuesGTEOutput(Transaction tx) {
        return sumOfInputs(tx.getInputs()) >= sumOfOutputs(tx.getOutputs());
    }

    private double sumOfInputs(Collection<Transaction.Input> inputs) {
        double sum = 0.0;
        for (Transaction.Input i : inputs) {
            UTXO utxo = new UTXO(i.prevTxHash, i.outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            sum += output.value;
        }
        return sum;
    }

    private double sumOfOutputs(Collection<Transaction.Output> outputs) {
        double sum = 0.0;
        for (Transaction.Output o : outputs) {
            sum += o.value;
        }
        return sum;
    }

}
