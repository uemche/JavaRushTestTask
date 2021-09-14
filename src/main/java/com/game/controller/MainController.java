package com.game.controller;


import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class MainController {
    private PlayerService playerService;

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping(path = "/players")
    public List<Player> getAllPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false)Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false) PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
            ){
        List<Player> players = playerService.getAllPlayers(name, title, race, profession, after, before, banned, minExperience,
                maxExperience, minLevel, maxLevel, order);
        return  playerService.getPage(pageNumber, pageSize, players);
    }

    @GetMapping(path = "/players/count")
    public Integer getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false)Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {
        return playerService.getAllPlayers(name, title, race, profession, after, before, banned, minExperience,
                maxExperience, minLevel, maxLevel, null).size();
    }
    @PostMapping(path = "/players")
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!playerService.isPlayerValid(player)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        int exp = player.getExperience();
        int level = playerService.computeLevel(exp);
        player.setLevel(level);
        player.setUntilNextLevel(playerService.computeUntilNextLevel(exp, level));
        Player savedPlayer = playerService.savePlayer(player);
        return new ResponseEntity<>(savedPlayer, HttpStatus.OK);

    }
    @GetMapping(path = "/players/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        if (id == null || id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Player player = playerService.getPlayerById(id);
        if (player == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }
    @PostMapping(path = "/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        ResponseEntity<Player> entity = getPlayerById(id);
        Player savedPlayer = entity.getBody();
        if (savedPlayer == null)
            return entity;
        Player result;
        try {
            result = playerService.updatePlayer(savedPlayer, player);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @DeleteMapping(path = "/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable Long id) {
        ResponseEntity<Player> entity = getPlayerById(id);
        Player player = entity.getBody();
        if (player == null)
            return entity;
        playerService.deletePlayer(player);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
