package com.github.timebetov.service.implementation;

import com.github.timebetov.models.Transaction;
import com.github.timebetov.service.TransactionService;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class InFilesTransactionService implements TransactionService {

    private final String username;
    private final Map<UUID, Long> indexedUUIDs;
    private final List<Transaction> transactions;

    private final Path dataPath;
    private final Path idxPath;

    public InFilesTransactionService(String username) {
        this.username = username;
        this.dataPath = Path.of("data", username+"_data.dat");
        this.idxPath = Path.of("data", username+"_data.idx");
        this.indexedUUIDs = new LinkedHashMap<>();
        this.transactions = new ArrayList<>();

        try {
            if (!Files.exists(dataPath.getParent()))
                Files.createDirectory(dataPath.getParent());
            if (!Files.exists(dataPath)) {
                Files.createFile(dataPath);

                // CASE: Get rid of old data
                Files.deleteIfExists(idxPath);
                Files.createFile(idxPath);
            }

            // Loading indexes and data from saved file
            loadIndex();
            loadTransactions();
        } catch (IOException ex) {
            // Ignore
        }
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void add(Transaction transaction) {

        if (indexedUUIDs.containsKey(transaction.getId()))
            throw new IllegalArgumentException("Transaction with ID: " + transaction.getId() + " already exists");

        try (
                RandomAccessFile da = new RandomAccessFile(dataPath.toString(), "rw");
                RandomAccessFile ia = new RandomAccessFile(idxPath.toString(), "rw")
        ) {
            long pos = writeTransaction(da, transaction, da.length());
            indexedUUIDs.put(transaction.getId(), pos);
            writeIndex(ia, transaction.getId(), pos);
            this.transactions.add(transaction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Transaction> getTransactions(boolean isDeleted) {
        return transactions.stream()
                .filter(t -> t.isDeleted() == isDeleted)
                .toList();
    }

    @Override
    public void update(String transactionId, Transaction transaction) {

        // FIXME: Update description does not work as expected cause size of String
        // SOLUTION:
        // STEP 1: Add it like a new record in file
        // STEP 2: Mark it as deleted or updated and not to load
        // STEP 3: Refresh indexes

        Transaction update = getById(transactionId);
        try (RandomAccessFile ra = new RandomAccessFile(dataPath.toString(), "rw")) {

            if (transaction.getType() != null)
                update.setType(transaction.getType());
            if (transaction.getCategory() != null)
                update.setCategory(transaction.getCategory());
            if (transaction.getAmount() != null)
                update.setAmount(transaction.getAmount());
            if (transaction.getDescription() != null)
                update.setDescription(transaction.getDescription());
            if (transaction.getTransactionTime() != null)
                update.setTransactionTime(transaction.getTransactionTime());

            long position = indexedUUIDs.get(UUID.fromString(transactionId));
            writeTransaction(ra, update, position);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String transactionId) {

        UUID id = UUID.fromString(transactionId);
        long position = indexedUUIDs.get(id);

        try (RandomAccessFile ra = new RandomAccessFile(dataPath.toString(), "rw")) {

            for (var transaction : transactions)
                if (transaction.getId().equals(id))
                    transaction.setDeleted(true);

            ra.seek(position);
            ra.writeBoolean(true);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public Transaction getById(String transactionId) {

        UUID id = UUID.fromString(transactionId);
        return transactions.stream()
                .filter(t -> !t.isDeleted())
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transaction with ID: " + transactionId + " not found"));
    }

    private long writeTransaction(RandomAccessFile ra, Transaction data, long position) throws IOException {

        // Pointer: End of the file
        ra.seek(position);
        long positionInFile = ra.getFilePointer();

        // Serializing & Writing actual data
        ra.writeBoolean(data.isDeleted());                          // boolean writes 1 byte
        ra.writeUTF(data.getType().toString());                     // size depends on character size
        ra.writeUTF(data.getCategory().toString());

        // Splitting & Writing BigDecimal
        BigDecimal amount = data.getAmount();
        ra.writeInt(amount.scale());                                // Scale of BigDecimal and int writes 4 bytes
        byte[] unscaledBytes = amount.unscaledValue().toByteArray();
        ra.writeInt(unscaledBytes.length);                          // Length of an array
        ra.write(unscaledBytes);                                    // Bytes

        ra.writeUTF(data.getDescription());                         // Description
        ra.writeLong(data.getTransactionTime().toEpochMilli());     // Timestamp UTC

        return positionInFile;
    }

    private void writeIndex(RandomAccessFile ra, UUID transactionId, long lastDataPosition) throws IOException {

        // Writing records length
        ra.seek(0);
        ra.writeInt(indexedUUIDs.size());

        ra.seek(ra.length());                                       // Moving pointer to end of Indexes

        ra.writeLong(transactionId.getMostSignificantBits());       // writing UUID.mostSignificantBits as Long
        ra.writeLong(transactionId.getLeastSignificantBits());      // writing UUID.leastSignificantBits as Long
        ra.writeLong(lastDataPosition);                             // writing position in file as Long
    }

    private Transaction readTransaction(RandomAccessFile ra, UUID id) throws IOException {

        long positionInFile = indexedUUIDs.get(id);
        ra.seek(positionInFile);

        boolean isDeleted = ra.readBoolean();
        var type = Transaction.TransactionType.valueOf(ra.readUTF());
        var category = Transaction.Category.valueOf(ra.readUTF());

        // Reading amount (BigDecimal)
        int scale = ra.readInt();
        int lengthOfBytes = ra.readInt();
        byte[] unscaledBytes = new byte[lengthOfBytes];
        ra.readFully(unscaledBytes);

        BigInteger unscaled = new BigInteger(unscaledBytes);
        BigDecimal amount = new BigDecimal(unscaled, scale);

        String description = ra.readUTF();
        Instant transactionTime = Instant.ofEpochMilli(ra.readLong());

        Transaction transaction = new Transaction(type, category, amount, description, transactionTime);
        transaction.setId(id);
        transaction.setDeleted(isDeleted);
        return transaction;
    }

    private void loadIndex() {

        try (RandomAccessFile ra = new RandomAccessFile(idxPath.toString(), "r")) {
            ra.seek(0);
            if (ra.length() < 4) return;
            int size = ra.readInt();

            for (int i = 0; i < size; i++) {
                if (ra.getFilePointer() + 24 > ra.length()) break; // 3 long = 24 bytes

                long most = ra.readLong();
                long least = ra.readLong();
                long positionInFile = ra.readLong();

                UUID key = new UUID(most, least);
                indexedUUIDs.put(key, positionInFile);
                System.out.println("\tINDEX LOADED SUCCESS >>> KEY >>> " + key + " Position >>> " + positionInFile);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Something went wrong when loading indexes: " + ex.getMessage());
        }
    }

    private void loadTransactions() {

        try (RandomAccessFile ra = new RandomAccessFile(dataPath.toString(), "r")) {

            indexedUUIDs.keySet().forEach(id -> {
                try {
                    transactions.add(readTransaction(ra, id));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException("Something went wrong when loading transactions: " + ex.getMessage());
        }
    }
}
