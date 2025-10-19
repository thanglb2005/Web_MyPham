package vn.util.chat;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility helpers for working with chat room identifiers.
 * Room formats introduced for shop specific chats:
 *  - shop-{shopId}-customer-{userId}
 *  - shop-{shopId}-guest-{guestKey}
 * Legacy formats will continue to be recognised for backward compatibility:
 *  - user-{userId}
 *  - user-{userId}-seller-{vendorId}
 */
public final class ChatRoomUtils {

    private static final Pattern SHOP_ROOM_PATTERN =
            Pattern.compile("^shop-(\\d+)-(customer|guest)-([A-Za-z0-9_-]+)$");

    private static final Pattern LEGACY_USER_ROOM_PATTERN =
            Pattern.compile("^user-(\\d+)(?:-seller-(\\d+))?$");

    // Liaison room patterns (CSKH ↔ Vendor/Shop)
    private static final Pattern LIAISON_VENDOR_PATTERN =
            Pattern.compile("^liaison-vendor-(\\d+)-cskh-(\\d+)$");

    private static final Pattern LIAISON_SHOP_PATTERN =
            Pattern.compile("^liaison-shop-(\\d+)-cskh-(\\d+)$");

    private ChatRoomUtils() {
        // utility
    }

    public static boolean isShopRoom(String roomId) {
        if (roomId == null) {
            return false;
        }
        return SHOP_ROOM_PATTERN.matcher(roomId).matches();
    }

    public static Optional<Long> extractShopId(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        Matcher matcher = SHOP_ROOM_PATTERN.matcher(roomId);
        if (matcher.matches()) {
            try {
                return Optional.of(Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static Optional<Long> extractCustomerId(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        Matcher matcher = SHOP_ROOM_PATTERN.matcher(roomId);
        if (matcher.matches() && "customer".equalsIgnoreCase(matcher.group(2))) {
            try {
                return Optional.of(Long.parseLong(matcher.group(3)));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        matcher = LEGACY_USER_ROOM_PATTERN.matcher(roomId);
        if (matcher.matches()) {
            try {
                return Optional.of(Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static Optional<Long> extractLegacyVendorId(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        Matcher matcher = LEGACY_USER_ROOM_PATTERN.matcher(roomId);
        if (matcher.matches() && matcher.group(2) != null) {
            try {
                return Optional.of(Long.parseLong(matcher.group(2)));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static Optional<String> extractGuestKey(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        Matcher matcher = SHOP_ROOM_PATTERN.matcher(roomId);
        if (matcher.matches() && "guest".equalsIgnoreCase(matcher.group(2))) {
            return Optional.ofNullable(matcher.group(3));
        }
        return Optional.empty();
    }

    public static String buildShopRoomId(Long shopId, Long customerId, String guestKey) {
        Objects.requireNonNull(shopId, "shopId is required");
        if (customerId == null && (guestKey == null || guestKey.isBlank())) {
            guestKey = randomGuestKey();
        }
        if (customerId != null) {
            return "shop-" + shopId + "-customer-" + customerId;
        }
        return "shop-" + shopId + "-guest-" + sanitizeGuestKey(guestKey);
    }

    private static String sanitizeGuestKey(String guestKey) {
        if (guestKey == null) {
            return randomGuestKey();
        }
        String normalized = guestKey.trim().replaceAll("[^A-Za-z0-9_-]", "");
        if (normalized.isEmpty()) {
            normalized = randomGuestKey();
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String randomGuestKey() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static boolean isLiaisonVendorRoom(String roomId) {
        if (roomId == null) return false;
        return LIAISON_VENDOR_PATTERN.matcher(roomId).matches();
    }

    public static boolean isLiaisonShopRoom(String roomId) {
        if (roomId == null) return false;
        return LIAISON_SHOP_PATTERN.matcher(roomId).matches();
    }

    public static Optional<Long> extractLiaisonVendorId(String roomId) {
        if (roomId == null) return Optional.empty();
        java.util.regex.Matcher m = LIAISON_VENDOR_PATTERN.matcher(roomId);
        if (m.matches()) {
            try { return Optional.of(Long.parseLong(m.group(1))); } catch (NumberFormatException ignore) {}
        }
        return Optional.empty();
    }

    public static Optional<Long> extractLiaisonShopId(String roomId) {
        if (roomId == null) return Optional.empty();
        java.util.regex.Matcher m = LIAISON_SHOP_PATTERN.matcher(roomId);
        if (m.matches()) {
            try { return Optional.of(Long.parseLong(m.group(1))); } catch (NumberFormatException ignore) {}
        }
        return Optional.empty();
    }

    public static Optional<Long> extractLiaisonCskhId(String roomId) {
        if (roomId == null) return Optional.empty();
        java.util.regex.Matcher m1 = LIAISON_VENDOR_PATTERN.matcher(roomId);
        if (m1.matches()) {
            try { return Optional.of(Long.parseLong(m1.group(2))); } catch (NumberFormatException ignore) {}
        }
        java.util.regex.Matcher m2 = LIAISON_SHOP_PATTERN.matcher(roomId);
        if (m2.matches()) {
            try { return Optional.of(Long.parseLong(m2.group(2))); } catch (NumberFormatException ignore) {}
        }
        return Optional.empty();
    }

    public static String buildLiaisonShopRoom(Long shopId, Long cskhId) {
        Objects.requireNonNull(shopId, "shopId is required");
        Objects.requireNonNull(cskhId, "cskhId is required");
        return "liaison-shop-" + shopId + "-cskh-" + cskhId;
    }
}
