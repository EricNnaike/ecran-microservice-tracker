package com.appService2.appService2;

import com.appService2.appService2.entity.Role;
import com.appService2.appService2.entity.User;
import com.appService2.appService2.repository.RoleRpository;
import com.appService2.appService2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
@SpringBootApplication
//@EnableEurekaServer
public class AppService2Application {

	public static void main(String[] args) {
		SpringApplication.run(AppService2Application.class, args);
	}

//	@Bean
//	public JavaMailSender mailSender() {
//		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//		mailSender.setHost("smtp.gmail.com");
//		mailSender.setPort(587);
//
//		mailSender.setUsername("almustaphatukur00@gmail.com");
//		mailSender.setPassword("wsscoulaqmeuxinv");
//
//		Properties props = mailSender.getJavaMailProperties();
//		props.put("mail.transport.protocol", "smtp");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.debug", "true");
//
//		return mailSender;
//	}
	@Value("${app.roles}")
	private String appRoles;

	@Autowired
	private  RoleRpository roleRpository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;



	@PostConstruct
	public void initialRoles() {

		String[] roles = appRoles.split( ",");
		for(String roleName : roles){
			Role role1 = roleRpository.findByRole(roleName).orElse(null);
			if (role1 == null){
				Role role = new Role();
				role.setRole(roleName);
				roleRpository.save(role);
			}

		}

	}

	@PostConstruct
	public void initSuperAdmin() {
		Role role1 = roleRpository.findByRole("SUPER_ADMIN").orElse(null);
		User user1 = userRepository.findByEmail("ccanazodo@gmail.com").orElse(null);
		User user = new User();
		if(user1 == null) {
			user.setEmail("ccanazodo@gmail.com");
			user.setState("ACTIVE");
			user.setStatus(true);
			user.setFirstName("Chinedu");
			user.setLastName("Anazodo");
			user.setPassword(passwordEncoder.encode("1234"));
			user.getRoles().add(role1);

			userRepository.save(user);

		}


	}

//	@PostConstruct
//	public void initAdmin() {
//		Role role2 = roleRpository.findByRole("ADMIN").orElse(null);
//		User user2 = userRepository.findByEmail("uchenna@gmail.com").orElse(null);
//		User user = new User();
//		if(user2 == null) {
//			user.setEmail("uchenna@gmail.com");
//			user.setState("ACTIVE");
//			user.setStatus(true);
//			user.setFirstName("Uchenna");
//			user.setLastName("Nnaike");
//			user.setPassword(passwordEncoder.encode("123"));
//			user.getRoles().add(role2);
//
//			userRepository.save(user);
//
//		}
//	}


}



