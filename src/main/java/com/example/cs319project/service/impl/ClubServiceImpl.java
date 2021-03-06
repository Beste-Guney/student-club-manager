package com.example.cs319project.service.impl;

import com.example.cs319project.dto.ClubDto;
import com.example.cs319project.model.Club;
import com.example.cs319project.model.Student;
import com.example.cs319project.repository.ClubRepository;
import com.example.cs319project.service.ClubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClubServiceImpl implements ClubService {

    private final ClubRepository repository;

    @Override
    public Club findById(Integer id) {
       return repository.findClubById(id);
    }


    @Override
    public Club createNewClub(Club club) {
        Objects.requireNonNull(club, "club cannot be null");
        return repository.save(club);
    }

    public void deleteClub(Club club) {
        repository.delete(club);
    }

    @Override
    public List<Club> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void updateClub(ClubDto dto) {
        Club origClub = repository.findClubById(dto.getId());
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true);
        mapper.map(dto, origClub);
        repository.save(origClub);
    }

    @Override
    public Club saveClub(Club club) {
        Club newClub = repository.findClubById(club.getId());
        return repository.save(newClub);
    }
}
