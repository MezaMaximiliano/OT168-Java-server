package com.alkemy.ong.data.gateways;

import com.alkemy.ong.domain.exceptions.ResourceNotFoundException;
import com.alkemy.ong.domain.members.Member;
import com.alkemy.ong.domain.members.MemberGateway;

import com.alkemy.ong.data.entities.MemberEntity;
import com.alkemy.ong.data.repositories.MemberRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class DefaultMemberGateway implements MemberGateway {

    private final MemberRepository memberRepository;

    public DefaultMemberGateway(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll().stream()
                .map(m -> toModel(m))
                .collect(toList());
    }

    @Override
    public Member save(Member member) {
        return toModel(memberRepository.save(toEntity(member)));
    }

    @Override
    public Member findById(Long id) {
        return toModel(memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("The ID doesn't exist.")));
    }

    @Override
    public void delete(Long id) {
        findById(id);
        memberRepository.deleteById(id);
    }

    @Override
    public Member update(Member member) {
        MemberEntity memberEntity = toEntity(findById(member.getId()));
        return toModel(memberRepository.save(toUpdate(memberEntity, member)));
    }

    private Member toModel(MemberEntity memberEntity) {
        return Member.builder()
                .id(memberEntity.getId())
                .name(memberEntity.getName())
                .facebookUrl(memberEntity.getFacebookUrl())
                .instagramUrl(memberEntity.getInstagramUrl())
                .linkedinUrl(memberEntity.getLinkedinUrl())
                .image(memberEntity.getImage())
                .description(memberEntity.getDescription())
                .createdAt(memberEntity.getCreatedAt())
                .updatedAt(memberEntity.getUpdatedAt())
                .deleted(memberEntity.getDeleted())
                .build();
    }

    private MemberEntity toEntity(Member member) {
        return MemberEntity.builder()
                .id(member.getId())
                .name(member.getName().trim())
                .facebookUrl(member.getFacebookUrl())
                .instagramUrl(member.getInstagramUrl())
                .linkedinUrl(member.getLinkedinUrl())
                .image(member.getImage())
                .description(member.getDescription())
                .createdAt(member.getCreatedAt())
                .build();
    }

    private MemberEntity toUpdate(MemberEntity memberEntity, Member member) {
        memberEntity.setName(member.getName().trim());
        memberEntity.setFacebookUrl(member.getFacebookUrl());
        memberEntity.setInstagramUrl(member.getInstagramUrl());
        memberEntity.setLinkedinUrl(member.getLinkedinUrl());
        memberEntity.setImage(member.getImage());
        memberEntity.setDescription(member.getDescription());
        return memberEntity;
    }
}