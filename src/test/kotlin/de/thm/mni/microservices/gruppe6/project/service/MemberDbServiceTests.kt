package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.project.model.persistence.Member
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.jms.core.JmsTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@ExtendWith(MockitoExtension::class)
class MemberDbServiceTests(
        @Mock private val memberRepository: MemberRepository,
        @Mock private val sender: JmsTemplate
) {

    //private val memberService = MemberDbService(memberRepository, sender)

    fun createTestMember(projectId: UUID, projectRole: String): Member {
        return Member(UUID.randomUUID(), projectId, UUID.randomUUID(), projectRole)
    }
    /*
    @Test
    fun testShouldReturnEmptyListOfMembers() {
        val projectId = UUID.randomUUID()
        given(memberRepository.getMembersByProjectID(projectId)).willReturn(Flux.fromIterable(emptyList()))
        val members: List<Member>? = memberService.getMembers(projectId).collectList().block()

        assertThat(members).isNotNull
        assertThat(members).isEmpty()
        assertThat(members).isEqualTo(emptyList<Project>())

        verify(memberRepository, times(1)).getMembersByProjectID(projectId)
    }

    @Test
    fun testShouldReturnAllMembers() {
        val projectId = UUID.randomUUID()
        val member1 = createTestMember(projectId, "admin")
        val member2 = createTestMember(projectId, "normal")
        val member3 = createTestMember(projectId, "normal")
        val memberList = listOf(member1, member2, member3)

        given(memberRepository.getMembersByProjectID(projectId)).willReturn(Flux.fromIterable(memberList))
        val members: List<Member>? = memberService.getMembers(projectId).collectList().block()

        assertThat(members).`as`("list of members of project with id $projectId").isNotNull
        assertThat(members).`as`("list of members of project with id $projectId").hasSize(memberList.size)
        members!!.withIndex().forEach { assertThat(it.value).`as`("member $it.index").isEqualTo(memberList[it.index]) }

        Mockito.verify(memberRepository, times(1)).getMembersByProjectID(projectId)
    }
*/
//    @Test
//    fun testShouldCreateMembers() {
//        val projectId = UUID.randomUUID()
//        val member1 = createTestMember(projectId, "admin")
//        member1.id = null
//        val member2 = createTestMember(projectId, "normal")
//        member2.id = null
//        val member3 = createTestMember(projectId, "normal")
//        member3.id = null
//        val memberList = listOf(member1, member2, member3)
//
//        val memberDTOList = memberList.map {
//            val memberDTO = MemberDTO()
//            memberDTO.projectRole = it.projectRole
//            memberDTO.userId = it.userId
//            memberDTO
//        }
//
//        val createdMemberList = memberList.map { it.copy(UUID.randomUUID()) }
//
//        memberList.withIndex().map{ given(memberRepository.save(it.value)).willReturn(Mono.just(createdMemberList[it.index])) }
//        val members: List<Member>? = memberService.createMembers(projectId, member1.userId, memberDTOList).collectList().block()
//
//        assertThat(members).`as`("list of created members of project with id $projectId").isNotNull
//        assertThat(members).`as`("list of created members of project with id $projectId").hasSize(createdMemberList.size)
//        members!!.withIndex().forEach { assertThat(it.value).`as`("member $it.index").isEqualTo(createdMemberList[it.index]) }
//
//        Mockito.verify(memberRepository, times(memberList.size)).save(any())
//    }
/*
    @Test
    fun shouldDeleteAllMembers() {
        val projectId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        given(memberRepository.deleteAllMembersByProjectID(projectId)).willReturn(Mono.empty())

        memberService.deleteAllMembers(projectId, userId).block()

        verify(memberRepository, times(1)).deleteAllMembersByProjectID(projectId)
    }

    @Test
    fun shouldDeleteMembers() {
        val projectId = UUID.randomUUID()
        val member1 = createTestMember(projectId, "admin")
        val member2 = createTestMember(projectId, "normal")
        val member3 = createTestMember(projectId, "normal")
        val memberList = listOf(member1, member2, member3)
        val memberIdList = memberList.map { it.userId }

        given(memberRepository.deleteMembersByProjectID(projectId, memberIdList)).willReturn(Mono.empty())

        memberService.deleteMembers(projectId, memberList).block()

        verify(memberRepository, times(1)).deleteMembersByProjectID(projectId, memberIdList)
    }

 */
}


