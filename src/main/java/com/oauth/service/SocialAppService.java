package com.oauth.service;

import com.oauth.entity.User;
import com.oauth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SocialAppService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(SocialAppService.class);

    public SocialAppService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String accessToken = userRequest.getAccessToken().getTokenValue();
        Integer githubId = oAuth2User.getAttribute("id");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        logger.info("GitHub User info: id={}, email={}, name={} token={}"
                , githubId, email, name, accessToken);


        logger.info("Checking if user exists in database.");
        Optional<User> optionalUser = userRepository.findByGithubId(githubId);

        User user;
        if (optionalUser.isEmpty()) {
            logger.info("User not found. Creating new user.");
            user = new User();
            user.setGithubId(githubId);
            user.setEmail(email);
            user.setName(name);
            user.setRole(oAuth2User.getAuthorities().toString());
            user.setAccessToken(accessToken);
        } else {
            logger.info("User found. Updating user.");
            user = optionalUser.get();
            user.setName(name);
            user.setEmail(email);
            user.setRole(oAuth2User.getAuthorities().toString());
            user.setAccessToken(accessToken);
        }
        userRepository.save(user);
        logger.info("User saved in database.");

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes(),
                "name"
        );
    }
}