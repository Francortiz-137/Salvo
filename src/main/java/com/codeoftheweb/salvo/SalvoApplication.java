package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.model.*;
import com.codeoftheweb.salvo.repository.*;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;
import com.codeoftheweb.salvo.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	GameService gameServ;

	@Autowired
	PlayerService playerServ;
	@Autowired
	GamePlayerService gamePlayerServ;

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(GameRepository gameRepo, PlayerRepository playerRepo,
									  GamePlayerRepository gamePlayerRepo, ShipRepository shipRepo,
									  SalvoRepository salvoRepo, ScoreRepository scoreRepo) {
		return (args) -> {

			LocalDateTime date = LocalDateTime.now();
			//Toy data for Players /
			Player player1 = new Player("Jack Bauer", "j.bauer@ctu.gov", passwordEncoder().encode("24"));
			Player player2 = new Player("Chloe O'Brian", "c.obrian@ctu.gov", passwordEncoder().encode("42"));
			Player player3 = new Player("Kim Bauer", "kim_bauer@gmail.com", passwordEncoder().encode("kb") );
			Player player4 = new Player("Tony Almeida","t.almeida@ctu.gov", passwordEncoder().encode("mole"));

			playerRepo.save(player1);
			playerRepo.save(player2);
			playerRepo.save(player3);
			playerRepo.save(player4);

            //Toy data for Games
			Game game1 = new Game(date);
			gameRepo.save(game1);
			Game game2 = new Game(date.plusHours(1));
			gameRepo.save(game2);
			Game game3 = new Game(date.plusHours(2));
			gameRepo.save(game3);
			Game game4 = new Game(date.plusHours(3));
			gameRepo.save(game4);
			Game game5 = new Game(date.plusHours(4));
			gameRepo.save(game5);
			Game game6 = new Game(date.plusHours(5));
			gameRepo.save(game6);
			Game game7 = new Game(date.plusHours(6));
			gameRepo.save(game7);
			Game game8 = new Game(date.plusHours(7));
			gameRepo.save(game8);


            //Toy data for GamePlayer
			LocalDateTime dateGp1 = LocalDateTime.now();
			LocalDateTime dateGp2 = LocalDateTime.now().plusHours(1).plusMinutes(30);
			LocalDateTime dateGp3 = LocalDateTime.now().plusHours(2).plusMinutes(30);
			LocalDateTime dateGp4 = LocalDateTime.now().plusHours(3).plusMinutes(30);
			LocalDateTime dateGp5 = LocalDateTime.now().plusHours(4).plusMinutes(30);
			LocalDateTime dateGp6 = LocalDateTime.now().plusHours(5).plusMinutes(30);
			LocalDateTime dateGp7 = LocalDateTime.now().plusHours(6).plusMinutes(30);
			LocalDateTime dateGp8 = LocalDateTime.now().plusHours(7).plusMinutes(30);

			//game1
			GamePlayer gmp1 = new GamePlayer(game1, player1, dateGp1);
			GamePlayer gmp2 = new GamePlayer(game1, player2, dateGp1);

			//game2
			GamePlayer gmp3 = new GamePlayer(game2, player1, dateGp2);
			GamePlayer gmp4 = new GamePlayer(game2, player2, dateGp2);

			//game3
			GamePlayer gmp5 = new GamePlayer(game3, player2, dateGp3);
			GamePlayer gmp6 = new GamePlayer(game3, player4, dateGp3);

			//game4
			GamePlayer gmp7 = new GamePlayer(game4, player2, dateGp4);
			GamePlayer gmp8 = new GamePlayer(game4, player1, dateGp4);

			//game5
			GamePlayer gmp9 = new GamePlayer(game5, player4, dateGp5);
			GamePlayer gmp10 = new GamePlayer(game5, player1, dateGp5);

			//game6
			GamePlayer gmp11 = new GamePlayer(game6, player3, dateGp6);

			//game7
			GamePlayer gmp12 = new GamePlayer(game7, player4, dateGp7);

			//game8
			GamePlayer gmp13 = new GamePlayer(game8, player3, dateGp8);
			GamePlayer gmp14 = new GamePlayer(game8, player4, dateGp8);


			gamePlayerRepo.save(gmp1);
			gamePlayerRepo.save(gmp2);
			gamePlayerRepo.save(gmp3);
			gamePlayerRepo.save(gmp4);
			gamePlayerRepo.save(gmp5);
			gamePlayerRepo.save(gmp6);
			gamePlayerRepo.save(gmp7);
			gamePlayerRepo.save(gmp8);
			gamePlayerRepo.save(gmp9);
			gamePlayerRepo.save(gmp10);
			gamePlayerRepo.save(gmp11);
			gamePlayerRepo.save(gmp12);
			gamePlayerRepo.save(gmp13);
			gamePlayerRepo.save(gmp14);

			//Toy data for ships
			// Game1
			Ship ship1 = new Ship("Destroyer", gmp1, Arrays.asList("H2", "H3", "H4"));
			Ship ship2 = new Ship("Submarine", gmp1, Arrays.asList("E1", "F1", "G1"));
			Ship ship3 = new Ship("Patrol Boat", gmp1, Arrays.asList("B4","B5"));
			Ship ship4 = new Ship("Destroyer", gmp2, Arrays.asList("B5","C5","D5"));
			Ship ship5 = new Ship("Patrol Boat", gmp2, Arrays.asList("F1","F2"));

			// Game2
			Ship ship6 = new Ship("Destroyer",gmp3, Arrays.asList("B5","C5","D5"));
			Ship ship7 = new Ship("Patrol Boat",gmp3, Arrays.asList("C6","C7"));
			Ship ship8 = new Ship("Submarine",gmp4, Arrays.asList("A2","A3","A4"));
			Ship ship9 = new Ship("Patrol Boat",gmp4, Arrays.asList("G6","H6"));

			// Game3
			Ship ship10 = new Ship("Destroyer",gmp5, Arrays.asList("B5","C5","D5"));
			Ship ship11 = new Ship("Patrol Boat",gmp5, Arrays.asList("C6","C7"));
			Ship ship12 = new Ship("Submarine",gmp6, Arrays.asList("A2","A3","A4"));
			Ship ship13 = new Ship("Patrol Boat",gmp6, Arrays.asList("G6,H6"));

			// Game4
			Ship ship14 = new Ship("Destroyer",gmp7, Arrays.asList("B5","C5","D5"));
			Ship ship15 = new Ship("Patrol Boat",gmp7, Arrays.asList("C6","C7"));
			Ship ship16 = new Ship("Submarine",gmp8, Arrays.asList("A2","A3","A4"));
			Ship ship17 = new Ship("Patrol Boat",gmp8, Arrays.asList("G6","H6"));

			// Game5
			Ship ship18 = new Ship("Destroyer",gmp9, Arrays.asList("B5","C5","D5"));
			Ship ship19 = new Ship("Patrol Boat",gmp9, Arrays.asList("C6","C7"));
			Ship ship20 = new Ship("Submarine",gmp10, Arrays.asList("A2","A3","A4"));
			Ship ship21 = new Ship("Patrol Boat",gmp10, Arrays.asList("G6","H6"));

			// Game6
			Ship ship22 = new Ship("Destroyer",gmp11, Arrays.asList("A2","A3","A4"));
			Ship ship23 = new Ship("Patrol Boat",gmp11, Arrays.asList("G6","H6"));

			// Game7
			Ship ship24 = new Ship("Destroyer",gmp13, Arrays.asList("A2","A3","A4"));
			Ship ship25 = new Ship("Patrol Boat",gmp13, Arrays.asList("G6","H6"));
			Ship ship26 = new Ship("Submarine",gmp14, Arrays.asList("A2","A3","A4"));
			Ship ship27 = new Ship("Patrol Boat",gmp14, Arrays.asList("G6","H6"));


			shipRepo.save(ship1);
			shipRepo.save(ship2);
			shipRepo.save(ship3);
			shipRepo.save(ship4);
			shipRepo.save(ship5);
			shipRepo.save(ship6);
			shipRepo.save(ship7);
			shipRepo.save(ship8);
			shipRepo.save(ship9);
			shipRepo.save(ship10);
			shipRepo.save(ship11);
			shipRepo.save(ship12);
			shipRepo.save(ship13);
			shipRepo.save(ship14);
			shipRepo.save(ship15);
			shipRepo.save(ship16);
			shipRepo.save(ship17);
			shipRepo.save(ship18);
			shipRepo.save(ship19);
			shipRepo.save(ship20);
			shipRepo.save(ship21);
			shipRepo.save(ship22);
			shipRepo.save(ship23);
			shipRepo.save(ship24);
			shipRepo.save(ship25);
			shipRepo.save(ship26);
			shipRepo.save(ship27);


			gmp1.addShip(ship1);
			gmp1.addShip(ship2);
			gmp1.addShip(ship3);

			gmp2.addShip(ship4);
			gmp2.addShip(ship5);

			gmp3.addShip(ship6);
			gmp3.addShip(ship7);

			gmp4.addShip(ship8);
			gmp4.addShip(ship9);

			gmp5.addShip(ship8);
			gmp5.addShip(ship9);

			gmp6.addShip(ship10);
			gmp6.addShip(ship11);

			gmp7.addShip(ship12);
			gmp7.addShip(ship13);

			gmp8.addShip(ship14);
			gmp8.addShip(ship15);

			gmp9.addShip(ship16);
			gmp9.addShip(ship17);

			gmp10.addShip(ship18);
			gmp10.addShip(ship19);

			gmp11.addShip(ship20);
			gmp11.addShip(ship21);

			gmp12.addShip(ship22);
			gmp12.addShip(ship23);

			gmp13.addShip(ship24);
			gmp13.addShip(ship25);

			gmp14.addShip(ship26);
			gmp14.addShip(ship27);

			gamePlayerRepo.save(gmp1);
			gamePlayerRepo.save(gmp2);
			gamePlayerRepo.save(gmp3);
			gamePlayerRepo.save(gmp4);
			gamePlayerRepo.save(gmp5);
			gamePlayerRepo.save(gmp6);
			gamePlayerRepo.save(gmp7);
			gamePlayerRepo.save(gmp8);
			gamePlayerRepo.save(gmp9);
			gamePlayerRepo.save(gmp10);
			gamePlayerRepo.save(gmp11);
			gamePlayerRepo.save(gmp12);
			gamePlayerRepo.save(gmp13);
			gamePlayerRepo.save(gmp14);

			//GAME1
			Salvo salvo1 = new Salvo(gmp1,Long.parseLong("1"),Arrays.asList("B5","C5","F1"));
			Salvo salvo2 = new Salvo(gmp1,Long.parseLong("2"),Arrays.asList("F2","D5"));
			Salvo salvo3 = new Salvo(gmp2,Long.parseLong("1"),Arrays.asList("B4","B5","B6"));
			Salvo salvo4 = new Salvo(gmp2,Long.parseLong("2"),Arrays.asList("E1","H3","A2"));

			//GAME2
			Salvo salvo5 = new Salvo(gmp3,Long.parseLong("1"),Arrays.asList("A2","A4","G6"));
			Salvo salvo6 = new Salvo(gmp3,Long.parseLong("2"),Arrays.asList("A3","H6"));
			Salvo salvo7 = new Salvo(gmp4,Long.parseLong("1"),Arrays.asList("B5","D5","C7"));
			Salvo salvo8 = new Salvo(gmp4,Long.parseLong("2"),Arrays.asList("C5","C6"));

			//GAME3
			Salvo salvo9 = new Salvo(gmp5,Long.parseLong("1"),Arrays.asList("G6","H6","A4"));
			Salvo salvo10 = new Salvo(gmp5,Long.parseLong("2"),Arrays.asList("A2","A3","D8"));
			Salvo salvo11 = new Salvo(gmp6,Long.parseLong("1"),Arrays.asList("H1","H2","H3"));
			Salvo salvo12 = new Salvo(gmp6,Long.parseLong("2"),Arrays.asList("E1","F2","G3"));

			//GAME4
			Salvo salvo13 = new Salvo(gmp7,Long.parseLong("1"),Arrays.asList("A3","A4","F7"));
			Salvo salvo14 = new Salvo(gmp7,Long.parseLong("2"),Arrays.asList("A2","G6","H6"));
			Salvo salvo15 = new Salvo(gmp8,Long.parseLong("1"),Arrays.asList("B5","C6","H1"));
			Salvo salvo16 = new Salvo(gmp8,Long.parseLong("2"),Arrays.asList("C5","C7","D5"));

			//GAME5
			Salvo salvo17 = new Salvo(gmp9,Long.parseLong("1"),Arrays.asList("A1","A2","A3"));
			Salvo salvo18 = new Salvo(gmp9,Long.parseLong("2"),Arrays.asList("G6","G7","EG8"));
			Salvo salvo19 = new Salvo(gmp10,Long.parseLong("1"),Arrays.asList("B5","B6","C7"));
			Salvo salvo20 = new Salvo(gmp10,Long.parseLong("2"),Arrays.asList("C6","D6","E6"));
			Salvo salvo21 = new Salvo(gmp10,Long.parseLong("3"),Arrays.asList("H1","H8"));



			salvoRepo.save(salvo1);
			salvoRepo.save(salvo2);
			salvoRepo.save(salvo3);
			salvoRepo.save(salvo4);
			salvoRepo.save(salvo5);
			salvoRepo.save(salvo6);
			salvoRepo.save(salvo7);
			salvoRepo.save(salvo8);
			salvoRepo.save(salvo9);
			salvoRepo.save(salvo10);
			salvoRepo.save(salvo11);
			salvoRepo.save(salvo12);
			salvoRepo.save(salvo13);
			salvoRepo.save(salvo14);
			salvoRepo.save(salvo15);
			salvoRepo.save(salvo16);
			salvoRepo.save(salvo17);
			salvoRepo.save(salvo18);
			salvoRepo.save(salvo19);
			salvoRepo.save(salvo20);
			salvoRepo.save(salvo21);


			gmp1.addSalvo(salvo1);
			gmp1.addSalvo(salvo2);

			gmp2.addSalvo(salvo3);
			gmp2.addSalvo(salvo4);

			gmp3.addSalvo(salvo5);
			gmp3.addSalvo(salvo6);

			gmp4.addSalvo(salvo7);
			gmp4.addSalvo(salvo8);

			gmp5.addSalvo(salvo9);
			gmp5.addSalvo(salvo10);

			gmp6.addSalvo(salvo11);
			gmp6.addSalvo(salvo12);

			gmp7.addSalvo(salvo13);
			gmp7.addSalvo(salvo14);

			gmp8.addSalvo(salvo15);
			gmp8.addSalvo(salvo16);

			gmp9.addSalvo(salvo17);
			gmp9.addSalvo(salvo18);

			gmp10.addSalvo(salvo19);
			gmp10.addSalvo(salvo20);
			gmp10.addSalvo(salvo21);

			gamePlayerRepo.save(gmp1);
			gamePlayerRepo.save(gmp2);
			gamePlayerRepo.save(gmp3);
			gamePlayerRepo.save(gmp4);
			gamePlayerRepo.save(gmp5);
			gamePlayerRepo.save(gmp6);
			gamePlayerRepo.save(gmp7);
			gamePlayerRepo.save(gmp8);
			gamePlayerRepo.save(gmp9);
			gamePlayerRepo.save(gmp10);



			Score scr1 = new Score(player1,game1,dateGp1.plusMinutes(30),1.0);
			Score scr2 = new Score(player2,game1,dateGp1.plusMinutes(30),0.0);

			Score scr3 = new Score(player1,game2,dateGp2.plusMinutes(30),0.5);
			Score scr4 = new Score(player2,game2,dateGp2.plusMinutes(30),0.5);

			scoreRepo.save(scr1);
			scoreRepo.save(scr2);
			scoreRepo.save(scr3);
			scoreRepo.save(scr4);

			player1.addScore(scr1);
			player1.addScore(scr3);
			player2.addScore(scr2);
			player2.addScore(scr4);

			playerRepo.save(player1);
			playerRepo.save(player2);

			game1.addScore(scr1);
			game1.addScore(scr2);
			game2.addScore(scr3);
			game2.addScore(scr4);

			gameRepo.save(game1);
			gameRepo.save(game2);

			/** para debugging
			List<Game> games = gameRepo.findAll();
			List<Player> player = playerRepo.findAll();
			List<GamePlayer> gamePlayers = gamePlayerRepo.findAll();


			List<Game> gamesServ = gameServ.findAll();
			List<Player> playersServ = playerServ.findAll();
			List<GamePlayer> gamePlayersServ = gamePlayerServ.findAll();
			 */
		};

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
	@Autowired
	PlayerService playerService;


	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {

		 auth.userDetailsService(inputName-> {
			 Player player = playerService.findByUserName(inputName);
			 if (player != null) {
				 return new User(player.getUserName(), player.getPassword(),
						 AuthorityUtils.createAuthorityList("USER"));
			 } else {
				 throw new UsernameNotFoundException("Unknown user: " + inputName);
			 }
		 });
	}



}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/api/games", "/api/players", "/api/login").permitAll()
				.antMatchers("/web/game.html").hasAuthority("USER")
				.antMatchers("/web/**").permitAll()
				.and()
				.formLogin()
				.usernameParameter("name")
				.passwordParameter("pwd")
				.loginPage("/api/login");


		http.logout().logoutUrl("/api/logout");

		// turn off checking for CSRF tokens
		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());


	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}

}
