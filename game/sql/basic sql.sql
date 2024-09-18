delete from game_players where game_id < 100000;
select * from game_players;
delete from game where id < 100000;
select * from game;		
delete from gamer where id < 100000;
select * from gamer;

drop database mafia;
create database mafia;