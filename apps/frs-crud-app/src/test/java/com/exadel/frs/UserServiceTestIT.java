package com.exadel.frs;

import com.exadel.frs.dto.ui.UserCreateDto;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.helpers.EmailSender;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.service.UserService;
import liquibase.integration.spring.SpringLiquibase;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("Tests that use database")
@ExtendWith(SpringExtension.class)
@DataJpaTest
@MockBeans({@MockBean(SpringLiquibase.class), @MockBean(PasswordEncoder.class), @MockBean(EmailSender.class)})
@Import({UserService.class})
public class UserServiceTestIT {

    private static final String ENABLED_USER_EMAIL = "enabled_user@email.com";
    private static final String DISABLED_USER_EMAIL = "disabled_user@email.com";

    @SpyBean
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getEnabledUserByEmailReturnsActiveUser() {
        createAndEnableUser(ENABLED_USER_EMAIL);

        val enabledUser = userService.getEnabledUserByEmail(ENABLED_USER_EMAIL);

        assertNotNull(enabledUser);
        assertTrue(enabledUser.isEnabled());
    }

    @Test
    void getEnabledUserByEmailThrowsExceptionIfUserIsDisabled() {
        createUser(DISABLED_USER_EMAIL);

        val disabledUser = userRepository.findByEmail(DISABLED_USER_EMAIL).get();

        assertNotNull(disabledUser);
        assertFalse(disabledUser.isEnabled());

        assertThrows(UserDoesNotExistException.class, () -> userService.getEnabledUserByEmail(DISABLED_USER_EMAIL));
    }

    private void createAndEnableUser(final String email) {
        val regToken = UUID.randomUUID().toString();
        when(userService.generateRegistrationToken()).thenReturn(regToken);
        createUser(email);
        confirmRegistration(regToken);
    }

    private void createUser(final String email) {
        val user = UserCreateDto.builder()
                                .email(email)
                                .firstName("first_name")
                                .lastName("last_name")
                                .password("password")
                                .build();

        userService.createUser(user);
    }

    private void confirmRegistration(final String regToken) {
        userService.confirmRegistration(regToken);
    }
}