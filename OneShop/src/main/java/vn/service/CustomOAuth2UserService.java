package vn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.entity.Role;
import vn.entity.User;
import vn.repository.RoleRepository;
import vn.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = oAuth2UserRequest.getClientRegistration().getRegistrationId(); // "facebook" or "google"
        
        // Get provider ID based on provider type
        String providerId;
        if ("google".equals(provider)) {
            providerId = oAuth2User.getAttribute("sub"); // Google uses "sub" for user ID
        } else {
            providerId = oAuth2User.getAttribute("id"); // Facebook uses "id"
        }

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // User exists - update OAuth2 info
            user = userOptional.get();
            user.setProvider(provider);
            user.setProviderId(providerId);
            userRepository.save(user);
        } else {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setEnabled(true);
            user.setPassword(""); // OAuth2 users don't need password
            
            // Set default values for OAuth2 users
            user.setAvatar("user.png"); // Default avatar
            user.setRegisterDate(java.sql.Date.valueOf(java.time.LocalDate.now())); // Current date
            user.setStatus(true); // Active status
            user.setOneXuBalance(0.0); // Default balance
            
            // Save user first (without roles)
            user = userRepository.save(user);
            
            // Then assign default USER role
            Set<Role> roles = new HashSet<>();
            Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
            if (userRole.isPresent()) {
                roles.add(userRole.get());
                user.setRoles(roles);
                userRepository.save(user); // Save again with roles
            }
        }
        
        return new CustomOAuth2User(oAuth2User, user);
    }
}
