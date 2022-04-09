package com.alkemy.ong.web.controllers;

import com.alkemy.ong.data.entities.RoleEntity;
import com.alkemy.ong.data.entities.UserEntity;
import com.alkemy.ong.data.repositories.RoleRepository;
import com.alkemy.ong.domain.exceptions.ResourceNotFoundException;
import com.alkemy.ong.web.controllers.AuthController.LoginDTO;
import com.alkemy.ong.web.controllers.AuthController.RegistrationDTO;
import com.alkemy.ong.web.controllers.AuthController.UserDTO;
import com.alkemy.ong.data.repositories.UserRepository;
import com.alkemy.ong.domain.security.jwt.JwtUtil;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;

    @MockBean
    UserDetailsService userDetailsService;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    RoleRepository roleRepository;

    @Autowired
    ObjectMapper mapper;

    @Test
    void loginSuccess() throws Exception {

        LoginDTO loginDTO = buildLoginDTO();
        Authentication authenticationTest = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        String token = buildToken(loginDTO.getEmail(),"USER");

        when(authenticationManager.authenticate(authenticationTest)).thenReturn(authenticationTest);
        when(jwtUtil.generateToken(buildUserDetails("USER",loginDTO.getEmail()))).thenReturn(token);

        mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt",is(token)));
    }

    @Test
    void loginNotFound() throws Exception {

        LoginDTO loginDTO = buildLoginDTO();

        when(userDetailsService.loadUserByUsername(loginDTO.getEmail())).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginDTO)))
                .andExpect(status().isNotFound());

        ResourceNotFoundException exceptionThrows = assertThrows(ResourceNotFoundException.class,
                () -> {userDetailsService.loadUserByUsername(loginDTO.getEmail());}, "User not found");

        Assertions.assertEquals("User not found", exceptionThrows.getMessage());
    }

    @Test
    void registerSuccess() throws Exception {
        UserEntity entityRequest = builUserdEntity(null,"USER");
        UserEntity entityResponse = builUserdEntity(1l,"USER");
        RoleEntity roleEntity = buildRole(2l,"USER");
        UserDTO userDTO = buildUserDTO(1l);
        RegistrationDTO registrationDTO = buildRegistrationDTO();
        Authentication authenticationTest = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registrationDTO.getEmail(), registrationDTO.getPassword()));

        String token = buildToken(registrationDTO.getEmail(),"USER");

        when(userRepository.save(entityRequest)).thenReturn(entityResponse);
        when(userRepository.findByEmail(entityRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findById(roleEntity.getId())).thenReturn(Optional.of(roleEntity));
        when(authenticationManager.authenticate(authenticationTest)).thenReturn(authenticationTest);
        when(jwtUtil.generateToken(buildUserDetails("USER",userDTO.getEmail()))).thenReturn(token);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.firstName",is("James")))
                .andExpect(jsonPath("$.lastName",is("Potter")))
                .andExpect(jsonPath("$.email",is("james@gmail.com")))
                .andExpect(jsonPath("$.photo",is("james.jpg")))
                .andExpect(header().string("Authorization",token));
    }

    @Test
    void registerBadRequestEmail() throws Exception{
        UserEntity entityRequest = builUserdEntity(null,"USER");
        UserEntity entityResponse = builUserdEntity(1l,"USER");
        RegistrationDTO registrationDTO = buildRegistrationDTO();

        when(userRepository.findByEmail(entityRequest.getEmail())).thenReturn(Optional.of(entityResponse));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists."));
    }

    @Test
    void registerBadRequestPasswordMatch() throws Exception{
        RegistrationDTO registrationDTO = buildRegistrationDTO();

        registrationDTO.setMatchingPassword("abcdefghi");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The passwords don't match."));
    }

    @Test
    void registerBadRequestRegisterField() throws Exception{
        RegistrationDTO registrationDTO = buildRegistrationDTO();

        registrationDTO.setFirstName("");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0]",is("The 'name' field is required.")));
    }

    @Test
    void meSuccess() throws Exception {
        UserDTO userDTO = buildUserDTO(1l);
        UserEntity userEntity = builUserdEntity(1l,"USER");
        RoleEntity roleEntity = buildRole(2l,"USER");
        Authentication authenticationTest = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getEmail(), "12345678"));

        String token = buildToken(userDTO.getEmail(),"USER");

        when(authenticationManager.authenticate(authenticationTest)).thenReturn(authenticationTest);
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(userEntity));
        when(jwtUtil.extractEmail(token)).thenReturn(userDTO.getEmail());
        when(jwtUtil.generateToken(buildUserDetails("User",userDTO.getEmail()))).thenReturn(token);

        mockMvc.perform(get("/auth/me").header("Authorization",token)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /*	public ResponseEntity<UserDTO> getAuthenticatedUserDetails(@RequestHeader(value = "Authorization") String authorizationHeader) {
		String email = jwtUtil.extractEmail(authorizationHeader);
		UserDTO useDTO = toDTO(userService.findByEmail(email));
		return ResponseEntity.ok().body(useDTO);
	}
	*/

    private RegistrationDTO buildRegistrationDTO() {
        return RegistrationDTO.builder()
                .firstName("James")
                .lastName("Potter")
                .email("james@gmail.com")
                .password("12345678")
                .matchingPassword("12345678")
                .photo("james.jpg")
                .build();
    }

    private LoginDTO buildLoginDTO(){
        return LoginDTO.builder()
                .email("james@gmail.com")
                .password("12345678")
                .build();
    }

    private UserEntity builUserdEntity(Long id,String roleName){
        return UserEntity.builder()
                .id(id)
                .firstName("James")
                .lastName("Potter")
                .email("james@gmail.com")
                .password("12345678")
                .photo("james.jpg")
                .createdAt(LocalDateTime.of(2022,03,29,18,58,56,555))
                .updatedAt(LocalDateTime.of(2022,03,29,18,58,56,555))
                .roleEntity(buildRole(2l,roleName))
                .build();
    }

    private UserDTO buildUserDTO(Long id) {
        return UserDTO.builder()
                .id(id)
                .firstName("James")
                .lastName("Potter")
                .email("james@gmail.com")
                .photo("james.jpg")
                .build();
    }

    private RoleEntity buildRole(Long id, String roleName){
        return RoleEntity.builder()
                .id(id)
                .name(roleName)
                .description("Normal user of the system")
                .createdAt(LocalDateTime.of(2022,03,29,18,58,56,555))
                .updatedAt(LocalDateTime.of(2022,03,29,18,58,56,555))
                .build();
    }

    private UserDetails buildUserDetails(String roleName,String userName){
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_"+roleName);
        return  new User(userName, "12345678", Collections.singletonList(authority));
    }

    private String buildToken(String email, String roleName ){
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_"+roleName);
        UserDetails userDetails = new User(email, "12345678", Collections.singletonList(authority));
        return jwtUtil.generateToken(userDetails);
    }
}
