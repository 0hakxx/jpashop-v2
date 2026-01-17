package jpabook.jpashop.api;


import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());

        memberService.join(member);
        return new CreateMemberResponse(member.getId());
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){
        Member member = memberService.findOne(id);
        member.setName(request.getName());
        memberService.update(id, request.getName());
        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> members = memberService.findMembers();
        List<MemberDto> collect = members.stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect);
    }


    @Data
    static class CreateMemberResponse{
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
    @Data
    private class CreateMemberRequest {
        private String name;
    }
    @Data
    private class UpdateMemberRequest {
        private String name;
    }

    @Data
    private class UpdateMemberResponse {
        private String name;
        private Long id;

        public UpdateMemberResponse(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    @Data
    @AllArgsConstructor
    private class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    private class MemberDto {
        private String name;
    }
}
