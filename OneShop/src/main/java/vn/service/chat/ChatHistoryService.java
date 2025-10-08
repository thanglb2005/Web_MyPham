package vn.service.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.nio.file.attribute.FileTime;

@Service
public class ChatHistoryService {
    private static final Path BASE_DIR = Paths.get("chat-data");
    private static final long MAX_UNCOMPRESSED_BYTES = 1_000_000; // ~1MB per active room log
    private static final int DEFAULT_LIMIT = 50;
    private static final int RETENTION_DAYS = 30; // rotate archives older than 30 days
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ObjectMapper mapper = new ObjectMapper();

    public ChatHistoryService() {
        try {
            if (!Files.exists(BASE_DIR)) {
                Files.createDirectories(BASE_DIR);
            }
        } catch (IOException ignored) {}
    }

    public void appendMessage(String roomId, Map<String, Object> message) {
        if (roomId == null || roomId.isEmpty() || message == null) return;
        Path file = roomFile(roomId);
        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }
            String json = mapper.writeValueAsString(message) + "\n";
            synchronized (getLock(file)) {
                Files.write(file, json.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                rotateIfNeeded(roomId, file);
            }
        } catch (IOException e) {
            // swallow to not impact chat flow
        }
    }

    public List<Map<String, Object>> getLastMessages(String roomId, Integer limit) {
        if (roomId == null || roomId.isEmpty()) return Collections.emptyList();
        int max = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, 500);
        Path file = roomFile(roomId);
        if (!Files.exists(file)) return Collections.emptyList();
        List<String> lines = tail(file, max);
        List<Map<String, Object>> result = new ArrayList<>(lines.size());
        for (String ln : lines) {
            try {
                Map<String, Object> m = mapper.readValue(ln, new TypeReference<Map<String, Object>>(){});
                result.add(m);
            } catch (Exception ignored) {}
        }
        // ensure ascending by sentAt
        result.sort(Comparator.comparingLong(o -> asLong(o.get("sentAt"))));
        return result;
    }

    /**
     * Delete persisted history for a room, including rotated gzip archives.
     * Returns true if any file was deleted.
     */
    public boolean deleteRoomHistory(String roomId) {
        if (roomId == null || roomId.isEmpty()) return false;
        Path file = roomFile(roomId);
        boolean deleted = false;
        try {
            synchronized (getLock(file)) {
                if (Files.deleteIfExists(file)) {
                    deleted = true;
                }
                // also delete any rotated archives for this room in the same directory
                Path dir = file.getParent();
                if (dir != null && Files.exists(dir)) {
                    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                        for (Path p : ds) {
                            String name = p.getFileName().toString();
                            if (name.startsWith(roomId + "-") && (name.endsWith(".log") || name.endsWith(".log.gz") || name.endsWith(".gz"))) {
                                try {
                                    if (Files.deleteIfExists(p)) {
                                        deleted = true;
                                    }
                                } catch (IOException ignored) {}
                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {}
        return deleted;
    }

    private long asLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    private Path roomFile(String roomId) {
        // spread into subdirs by prefix to avoid too many files in one dir
        String prefix = roomId.length() >= 2 ? roomId.substring(0, 2) : "xx";
        return BASE_DIR.resolve(prefix).resolve(roomId + ".log");
    }

    private void rotateIfNeeded(String roomId, Path file) {
        try {
            long size = Files.size(file);
            if (size < MAX_UNCOMPRESSED_BYTES) return;

            String ts = LocalDateTime.now().format(TS_FMT);
            Path rotated = file.resolveSibling(roomId + "-" + ts + ".log");
            Files.move(file, rotated, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            // Compress rotated file
            Path gz = rotated.resolveSibling(rotated.getFileName().toString() + ".gz");
            try (OutputStream os = Files.newOutputStream(gz, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                 GZIPOutputStream gos = new GZIPOutputStream(os, true);
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gos, StandardCharsets.UTF_8))) {
                Files.lines(rotated, StandardCharsets.UTF_8).forEach(l -> {
                    try { bw.write(l); bw.newLine(); } catch (IOException ignored) {}
                });
            }
            Files.deleteIfExists(rotated);

            cleanupOldArchives(file.getParent());
        } catch (IOException ignored) {}
    }

    private void cleanupOldArchives(Path dir) {
        long cutoff = System.currentTimeMillis() - RETENTION_DAYS * 24L * 3600_000L;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, path -> path.getFileName().toString().endsWith(".gz"))) {
            for (Path p : ds) {
                try {
                    FileTime ft = Files.getLastModifiedTime(p);
                    if (ft.toMillis() < cutoff) {
                        Files.deleteIfExists(p);
                    }
                } catch (IOException ignored) {}
            }
        } catch (IOException ignored) {}
    }

    private List<String> tail(Path file, int maxLines) {
        List<String> lines = new ArrayList<>(maxLines);
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long fileLength = raf.length();
            long pos = fileLength - 1;
            int lineCount = 0;
            StringBuilder sb = new StringBuilder();
            while (pos >= 0 && lineCount < maxLines) {
                raf.seek(pos--);
                int c = raf.read();
                if (c == '\n') {
                    if (sb.length() > 0) {
                        lines.add(sb.reverse().toString());
                        sb.setLength(0);
                        lineCount++;
                    }
                } else if (c != '\r') {
                    sb.append((char) c);
                }
            }
            if (sb.length() > 0 && lineCount < maxLines) {
                lines.add(sb.reverse().toString());
            }
        } catch (IOException ignored) {}
        Collections.reverse(lines);
        return lines;
    }

    // lightweight lock per file path
    private static final Map<Path, Object> LOCKS = new WeakHashMap<>();
    private synchronized Object getLock(Path p) {
        return LOCKS.computeIfAbsent(p.toAbsolutePath(), k -> new Object());
    }
}
