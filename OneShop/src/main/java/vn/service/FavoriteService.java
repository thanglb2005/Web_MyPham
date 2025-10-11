package vn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Favorite;
import vn.entity.Product;
import vn.entity.User;
import vn.repository.FavoriteRepository;
import vn.repository.ProductRepository;
import vn.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for Favorite functionality
 * @author OneShop Team
 */
@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Add product to favorites
     */
    public boolean addToFavorites(Long productId, Long userId) {
        try {
            // Check if already in favorites
            if (favoriteRepository.existsByProductIdAndUserId(productId, userId)) {
                return false; // Already in favorites
            }

            Optional<Product> productOpt = productRepository.findById(productId);
            Optional<User> userOpt = userRepository.findById(userId);

            if (productOpt.isPresent() && userOpt.isPresent()) {
                Favorite favorite = new Favorite();
                favorite.setProduct(productOpt.get());
                favorite.setUser(userOpt.get());
                favoriteRepository.save(favorite);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove product from favorites
     */
    @Transactional
    public boolean removeFromFavorites(Long productId, Long userId) {
        try {
            favoriteRepository.deleteByProductIdAndUserId(productId, userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Toggle favorite status
     */
    @Transactional
    public boolean toggleFavorite(Long productId, Long userId) {
        try {
            if (favoriteRepository.existsByProductIdAndUserId(productId, userId)) {
                favoriteRepository.deleteByProductIdAndUserId(productId, userId);
                return true;
            } else {
                return addToFavorites(productId, userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all favorites by user
     */
    public List<Favorite> getFavoritesByUser(Long userId) {
        return favoriteRepository.findAllByUserId(userId);
    }

    /**
     * Check if product is favorited by user
     */
    public boolean isFavorited(Long productId, Long userId) {
        return favoriteRepository.existsByProductIdAndUserId(productId, userId);
    }

    /**
     * Get favorite count by user
     */
    public Integer getFavoriteCountByUser(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }
}

