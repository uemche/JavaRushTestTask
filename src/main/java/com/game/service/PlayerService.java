package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PlayerService {
    private PlayerRepository playerRepository;

    @Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    public List<Player> getAllPlayers(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel,
            PlayerOrder order
    ) {
        Date afterDate = after == null ? null : new Date(after);
        Date beforeDate = before == null ? null : new Date(before);
        List<Player> players = new ArrayList<>();
        playerRepository.findAll().forEach(player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && !player.getBirthday().after(afterDate)) return;
            if (before != null && !player.getBirthday().before(beforeDate)) return;
            if (banned != null && player.getBanned() != banned) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            players.add(player);
        });
        if (order != null) {
            players.sort((player1, player2) -> {
                switch(order) {
                    case ID: return player1.getId().compareTo(player2.getId());
                    case NAME: return player1.getName().compareTo(player2.getName());
                    case LEVEL: return player1.getLevel().compareTo(player2.getLevel());
                    case BIRTHDAY: return player1.getBirthday().compareTo(player2.getBirthday());
                    case EXPERIENCE: return player1.getExperience().compareTo(player2.getExperience());
                    default: return 0;
                }
            });
        }
        return players;
    }
    public List<Player> getPage(Integer pageNumber, Integer pageSize, List<Player> players) {
        Integer page = pageNumber == null ? 0 : pageNumber;
        Integer size = pageSize == null ? 3 : pageSize;
        int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }
    public Integer computeLevel(Integer experience) {
        return (int) Math.floor((Math.sqrt(2500+200*experience) - 50) / 100);
    }
    public Integer computeUntilNextLevel(Integer experience, Integer level) {
        return 50 * (level + 1) * (level + 2) - experience;
    }
    public boolean isPlayerValid(Player player) {
        return player != null && isNameValid(player.getName()) && isTitleValid(player.getTitle())
                && isExperienceValid(player.getExperience()) && isBirthdayValid(player.getBirthday());
    }

    private boolean isBirthdayValid(Date birthday) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2000);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 3000);
        Date endDate = calendar.getTime();
        return birthday != null && birthday.after(startDate) && birthday.before(endDate);
    }

    private boolean isExperienceValid(Integer experience) {
        return experience != null && experience >= 0 && experience <= 10000000;
    }

    private boolean isTitleValid(String title) {
        return title != null && !title.isEmpty() && title.length() <= 30;
    }

    private boolean isNameValid(String name) {
        return name != null && name.length() <= 12 && !name.isEmpty();
    }

    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException {
        String name = newPlayer.getName();
        if (name != null) {
            if (!isNameValid(name))
                throw new IllegalArgumentException();
            else
                oldPlayer.setName(name);
        }
        String title = newPlayer.getTitle();
        if (title != null) {
            if (!isTitleValid(title))
                throw new IllegalArgumentException();
            else
                oldPlayer.setTitle(title);
        }
        Race race = newPlayer.getRace();
        if (race != null)
            oldPlayer.setRace(race);
        Profession profession = newPlayer.getProfession();
        if (profession != null)
            oldPlayer.setProfession(profession);
        Integer experience = newPlayer.getExperience();
        if (experience != null) {
            if (!isExperienceValid(experience))
                throw new IllegalArgumentException();
            else {
                oldPlayer.setExperience(experience);
                Integer level = computeLevel(experience);
                oldPlayer.setLevel(level);
                oldPlayer.setUntilNextLevel(computeUntilNextLevel(experience, level));
            }
        }
        Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
            if (!isBirthdayValid(birthday))
                throw new IllegalArgumentException();
            else
                oldPlayer.setBirthday(birthday);
        }
        Boolean banned = newPlayer.getBanned();
        if (banned != null)
            oldPlayer.setBanned(banned);
        playerRepository.save(oldPlayer);
        return oldPlayer;
    }
}
