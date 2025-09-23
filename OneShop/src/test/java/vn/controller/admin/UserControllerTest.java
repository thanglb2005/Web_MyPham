package vn.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class cho UserController
 * @author OneShop Team
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testUsersPageRedirectWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testUserDetailPageRedirectWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/admin/users/detail/1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testSearchUsersPageRedirectWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/admin/users/search").param("keyword", "test"))
                .andExpect(status().is3xxRedirection());
    }
}
