package com.lawnroad.account.controller;
import com.lawnroad.account.dto.*;
import com.lawnroad.account.entity.AdminEntity;
import com.lawnroad.account.entity.ClientEntity;
import com.lawnroad.account.entity.LawyerEntity;
import com.lawnroad.account.entity.UserEntity;
import com.lawnroad.account.mapper.AdminMapper;
import com.lawnroad.account.mapper.ClientMapper;
import com.lawnroad.account.mapper.LawyerMapper;

import com.lawnroad.account.mapper.UserMapper;
import com.lawnroad.account.service.ClientService;
import com.lawnroad.account.service.LawyerService;
import com.lawnroad.account.service.RefreshTokenService;
import com.lawnroad.common.util.JwtTokenUtil;
import com.lawnroad.common.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private LawyerService lawyerService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final ClientService clientService;
    private final UserContext userContext;
    private final ClientMapper clientMapper;
    private final RefreshTokenService refreshTokenService;
    private final JdbcTemplate jdbcTemplate;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/auth/check-id")
    public ResponseEntity<Map<String, Object>> checkIdDuplicate(@RequestParam String clientId) {  // 여기는 클라이언트 아이디를 중복 확인 하는 함수
        boolean available = clientService.isClientIdAvailable(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response); // 요청 성공했고, 이 데이터 줄게!  200일 때만 적용됨
    }

    @GetMapping("/auth/lawyer_check-id")
    public ResponseEntity<Map<String, Object>> checkLawyerIdDuplicate(@RequestParam String lawyerId) { // 여기는 변호사 아이디를 중복 확인 하는 함수
        boolean available = lawyerService.isLawyerAvailable(lawyerId);

        Map<String, Object> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response); // 요청 성공했고, 이 데이터 줄게!  200일 때만 적용됨
    }

    @GetMapping("/auth/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickNameDuplicate(@RequestParam String nickname) {
        boolean available = clientService.isClientNickNameAvailable(nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response); // 요청 성공했고, 이 데이터 줄게!  200일 때만 적용됨
    }


    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@RequestBody ClientSignupRequest request) {
        clientService.registerClient(request);
        return ResponseEntity.ok().body("회원가입 완료");
    }

//    @PostMapping("/auth/lawyer_signup")
//    public ResponseEntity<?> lawyer_signup(@RequestBody LawyerSignupRequest request) {
//        lawyerService.registerLawyer(request);
//        return ResponseEntity.ok().body("변호사 회원가입 완료");
//    }

//    @PostMapping(value = "/signuplawyer", consumes = "multipart/form-data")
//    public ResponseEntity<?> lawyerSignup(
//            @ModelAttribute LawyerSignupRequest request,
//            @RequestPart("profileImage") MultipartFile profileImage,
//            @RequestPart("idCardFront") MultipartFile idCardFront,
//            @RequestPart("idCardBack") MultipartFile idCardBack
//    ) {
//        lawyerService.registerLawyer(request, profileImage, idCardFront, idCardBack);
//        return ResponseEntity.ok("변호사 회원가입 완료");
//    }
@PostMapping(
        value = "/signuplawyer",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE

)
public ResponseEntity<?> lawyerSignup(
        @ModelAttribute LawyerSignupRequest request
) {
    System.out.println("변호사 회원가입 진입!!");
    lawyerService.registerLawyer(
            request,
            request.getProfileImage(),
            request.getIdCardFront(),
            request.getIdCardBack()
    );
    return ResponseEntity.ok("변호사 회원가입 완료");
}



    @GetMapping("/auth/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailDuplicate(@RequestParam String email) {
        boolean available = clientService.isEmailAvailable(email);

        Map<String, Object> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response);
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        try {
//            ClientEntity client = clientService.login(request.getClientId(), request.getPassword());
//
//            //ClientEntity client = clientMapper.findByClientId(clientId);
//            UserEntity user = userMapper.findByNo(client.getNo());
//            String accessToken = jwtTokenUtil.generateAccessToken(client.getClientId(),client.getNo(),user.getType(),client.getNickname());
//            String refreshToken = jwtTokenUtil.generateRefreshToken(client.getClientId());
//
//            // 🔍 여기에서 확인
//            System.out.println("✅ Access Token: " + accessToken);
//            //jwtTokenUtil.printPayload(accessToken); // 👈 payload 출력
//
//
//            jwtTokenUtil.storeRefreshToken(client.getClientId(), refreshToken);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("accessToken", accessToken);
//            result.put("refreshToken", refreshToken);
//            result.put("name", client.getName());
//            result.put("nickname", client.getNickname());
//            result.put("role", user.getType());
//
//            return ResponseEntity.ok(result);
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
//        }
//    }

//    @PostMapping("/auth/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        try {
//            System.out.println("💡 [전체 로그인 요청 도착] clientId: " + request.getClientId());
//            System.out.println("💡 [전체 로그인 요청 도착] type: " + request.getType());
//
//            String type = request.getType();
//            if (type == null) {
//                return ResponseEntity.badRequest().body("사용자 유형이 지정되지 않았습니다.");
//            }
//
//            if (type.equalsIgnoreCase("CLIENT")) {
//                ClientEntity client = clientService.login(request.getClientId(), request.getPassword());
//                UserEntity user = userMapper.findByNo(client.getNo());
//
//                String accessToken = jwtTokenUtil.generateAccessToken(client.getClientId(), client.getNo(), user.getType(), client.getNickname());
//                String refreshToken = jwtTokenUtil.generateRefreshToken(client.getClientId());
//                jwtTokenUtil.storeRefreshToken(client.getClientId(), refreshToken);
//
//                jwtTokenUtil.printPayload(accessToken);
//
//
//                System.out.println("accessToken : " + accessToken);
//                System.out.println("refreshToken : " + refreshToken);
//                Map<String, Object> result = new HashMap<>();
//                result.put("accessToken", accessToken);
//                result.put("refreshToken", refreshToken);
//                result.put("name", client.getName());
//                result.put("nickname", client.getNickname());
//                result.put("no",user.getNo());
//                result.put("role", user.getType());
//                return ResponseEntity.ok(result);
//
//            }
//
//            else if (type.equalsIgnoreCase("lawyer")) {
//                // 🔽 LawyerService 에 login 함수 구현 필요
//                LawyerEntity lawyer = lawyerService.login(request.getClientId(), request.getPassword());
//                UserEntity user = userMapper.findByNo(lawyer.getNo());
//                System.out.println("dfdfsdfksdfjkhdsksdjkhfjkdshfjkdhf");
//
//                System.out.println("로그인 요청: " + request.getClientId() + ", " + request.getType());
//                System.out.println("lawyer.getNo(): " + lawyer.getNo());
//
//                String accessToken = jwtTokenUtil.generateAccessToken(lawyer.getLawyerId(), lawyer.getNo(), user.getType(), lawyer.getName());
//                String refreshToken = jwtTokenUtil.generateRefreshToken(lawyer.getLawyerId());
//                jwtTokenUtil.storeRefreshToken(lawyer.getLawyerId(), refreshToken);
//                jwtTokenUtil.printPayload(accessToken);
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("accessToken", accessToken);
//                result.put("refreshToken", refreshToken);
//                result.put("name", lawyer.getName());
//                result.put("nickname", lawyer.getName()); // nickname 필드 없으면 name 대체
//                result.put("role", user.getType());
//
//                return ResponseEntity.ok(result);
//            }
//
//            return ResponseEntity.badRequest().body("알 수 없는 사용자 유형입니다.");
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
//        }
//    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String type = request.getType();
        if (type == null) {
            return ResponseEntity.badRequest().body("사용자 유형이 지정되지 않았습니다.");
        }

        Long   userNo;
        String clientId;
        String accessToken;
        String refreshToken;
        Map<String, Object> result = new HashMap<>();

        if (type.equalsIgnoreCase("CLIENT")) {
            ClientEntity client = clientService.login(request.getClientId(), request.getPassword());
            userNo   = client.getNo();
            clientId = client.getClientId();

            accessToken  = jwtTokenUtil.generateAccessToken(clientId,  userNo, /*role*/"CLIENT", client.getNickname(),client.getPhone());
            refreshToken = jwtTokenUtil.generateRefreshToken(clientId);

            result.put("name",     client.getName());
            result.put("nickname", client.getNickname());
            result.put("no",       client.getNo());
            result.put("phone",     client.getPhone());
        }
        else if (type.equalsIgnoreCase("LAWYER")) {
            LawyerEntity lawyer = lawyerService.login(request.getClientId(), request.getPassword());
            userNo   = lawyer.getNo();
            clientId = lawyer.getLawyerId();

            accessToken  = jwtTokenUtil.generateAccessToken(clientId,  userNo, /*role*/"LAWYER", lawyer.getName(),lawyer.getPhone());
            refreshToken = jwtTokenUtil.generateRefreshToken(clientId);

            result.put("name",     lawyer.getName());
            result.put("nickname", lawyer.getName());
            result.put("no", lawyer.getNo());
            result.put("phone",lawyer.getPhone());
        }
        else {
            return ResponseEntity.badRequest().body("알 수 없는 사용자 유형입니다.");
        }

        // ✏️ 여기서 DB에 저장만 하면 끝
        refreshTokenService.save(userNo, refreshToken);

        // 응답 페이로드
        result.put("accessToken",  accessToken);
        result.put("refreshToken", refreshToken);
        result.put("no",           userNo);
        result.put("role",         type.toUpperCase());

        return ResponseEntity.ok(result);
    }







    //아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<?> findClientId(@RequestBody FindIdRequest request) {
//        String clientId = clientService.findClientId(request.getFullName(), request.getEmail());
////        String lawyerId = lawyerService.findLawyerId(request.getFullName(),request.getEmail());
////
////        if (clientId == null) {
////            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 아이디를 찾을 수 없습니다.");
////        }
////
////        return ResponseEntity.ok(Map.of("clientId", clientId));
        String clientId = clientService.findClientId(request.getFullName(), request.getEmail());
        String lawyerId = lawyerService.findLawyerId(request.getFullName(), request.getEmail());
        System.out.println("아이디 찾기 여기까지 들어옴");
        System.out.println(clientId);
        System.out.println(lawyerId);

        if (clientId == null && lawyerId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 계정을 찾을 수 없습니다.");
        }

        Map<String, Object> result = new HashMap<>();
        if (clientId != null) result.put("clientId", clientId);
        if (lawyerId != null) result.put("lawyerId", lawyerId);
        System.out.println(clientId);
        System.out.println(lawyerId);

        return ResponseEntity.ok(result);
    }

//    //비번찾기
//    @PostMapping("/reset-password")
//    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
//        boolean success = clientService.resetPassword(request.getEmail(), request.getNewPassword());
//
//        if (!success) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 이메일로 등록된 계정이 없습니다.");
//        }
//
//        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
//    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success;

        if ("client".equalsIgnoreCase(request.getUserType())) {
            success = clientService.resetPassword(request.getUserId(), request.getEmail(), request.getFullName(), request.getNewPassword());
        } else if ("lawyer".equalsIgnoreCase(request.getUserType())) {
            success = lawyerService.resetPassword(request.getUserId(), request.getEmail(), request.getFullName(), request.getNewPassword());
        } else {
            return ResponseEntity.badRequest().body("잘못된 사용자 유형입니다.");
        }

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 계정이 없습니다.");
        }

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

//    @PutMapping("/client/nickname")
//    @PreAuthorize("hasRole('CLIENT')")
//    public ResponseEntity<?> updateNickname(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestBody Map<String, String> request
//    ) {
//        String token = authHeader.replace("Bearer ", "");
//        Claims claims = jwtTokenUtil.parseToken(token);
//        System.out.println("✅ 닉네임 수정 컨트롤러 진입");
//
//        String clientId = claims.getSubject(); // sub → clientId
//        String newNickname = request.get("nickname");
//
//        if (newNickname == null || newNickname.trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("닉네임은 필수입니다.");
//        }
//
//        clientService.updateNicknameByClientId(clientId, newNickname);
//        return ResponseEntity.ok().build();
//    }


//    @PutMapping("/lawyer/info")
//    //@PreAuthorize("hasRole('LAWYER')")
//    public ResponseEntity<?> updateLawyerInfo(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestBody Map<String, String> request
//    ) {
//        String token = authHeader.replace("Bearer ", "");
//        Claims claims = jwtTokenUtil.parseToken(token);
//        String lawyerId = claims.getSubject();
//
//        String officeNumber = request.get("officeNumber");
//        String phone = request.get("phone");
//        String detailAddress = request.get("detailAddress");
//
//        lawyerService.updateLawyerInfo(lawyerId, officeNumber, phone, detailAddress);
//        return ResponseEntity.ok().build();
//    }


    @GetMapping("/client/profile")
    public ResponseEntity<ClientProfileDTO> getClientProfile(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token    = authHeader.replace("Bearer ", "");
        Claims claims   = jwtTokenUtil.parseToken(token);
        String clientId = claims.getSubject();

        System.out.println("GETMAPPING");

        ClientProfileDTO dto = clientService.fetchClientProfile(clientId);
        return ResponseEntity.ok(dto);
    }



    @PutMapping("/client/profile")
    //@PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> updateClientProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request
    ) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token);

        String clientId = claims.getSubject();
        String nickname = request.get("nickname");
        String email = request.get("email");
        String phone = request.get("phone");

        if (nickname == null || email == null || phone == null ||
                nickname.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("모든 필드를 입력해주세요.");
        }

        clientService.updateClientProfile(clientId, nickname, email, phone);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/client/withdraw")
    //@PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> withdrawClient(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token);
        String clientId = claims.getSubject();

        clientService.withdrawClient(clientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        Long userNo = request.getUserNo(); // 프론트에서 전달받음
        System.out.println("로그 아웃" + userNo);
        refreshTokenService.deleteByUserNo(userNo);
        return ResponseEntity.ok("로그아웃 완료 (토큰 삭제)");
    }





//    @GetMapping("/refresh")
//    public ResponseEntity<?> refreshAccessToken(@RequestParam("no") Long no) {
//        System.out.println("🔄 [리프레시 요청] 사용자 no: " + no);
//
//        try {
//            // DB에서 사용자 정보 조회
//            String sql = "SELECT client_id, role, nickname FROM client WHERE no = ?";
//            Map<String, Object> user = jdbcTemplate.queryForMap(sql, no);
//
//            String clientId = (String) user.get("client_id");
//            String role = (String) user.get("role");
//            String nickname = (String) user.get("nickname");
//
//            // accessToken 발급
//            String newAccessToken = jwtTokenUtil.generateAccessToken(clientId, no, role, nickname);
//            System.out.println("✅ 재발급 완료: " + newAccessToken);
//
//            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
//        } catch (EmptyResultDataAccessException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 없음");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("재발급 오류");
//        }
//    }


//    @PostMapping("/refresh")
//    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, Object> payload) {
//        Long no = Long.valueOf(payload.get("no").toString());
//        System.out.println("🔄 [리프레시 요청] 사용자 no: " + no);
//
//        try {
//            // ✅ user.type을 role로 alias 지정
//            String sql = "SELECT c.client_id, c.nickname, u.type AS role " +
//                    "FROM client c " +
//                    "JOIN user u ON c.no = u.no " +
//                    "WHERE c.no = ?";
//
//            Map<String, Object> user = jdbcTemplate.queryForMap(sql, no);
//            System.out.println("✅ 쿼리문 통과");
//
//            String clientId = (String) user.get("client_id");
//            String nickname = (String) user.get("nickname");
//            String role = (String) user.get("role");  // u.type을 role로 사용
//            System.out.println("🎯 사용자 정보: " + clientId + " / " + role + " / " + nickname);
//
//            // ✅ accessToken 재발급
//            String newAccessToken = jwtTokenUtil.generateAccessToken(clientId, no, role, nickname);
//            System.out.println("✅ 재발급 완료: " + newAccessToken);
//
//            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
//        } catch (EmptyResultDataAccessException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 사용자 없음");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ 토큰 재발급 실패");
//        }
//    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, Object> payload) {
        Long no = Long.valueOf(payload.get("no").toString());
        System.out.println("\n🔄 [리프레시 요청] 사용자 no: " + no);

        try {
            // 1. user 테이블에서 type 조회
            String userSql = "SELECT type FROM user WHERE no = ?";
            String role = jdbcTemplate.queryForObject(userSql, String.class, no);
//            System.out.println("✅ 사용자 role: " + role);

            String id;
            String nickname = "";  // 기본값 비어있음
            String phone = "";

            // 2. role에 따라 client 또는 lawyer 테이블에서 정보 조회
            if ("CLIENT".equalsIgnoreCase(role)) {
                String clientSql = "SELECT client_id, nickname FROM client WHERE no = ?";
                Map<String, Object> client = jdbcTemplate.queryForMap(clientSql, no);
                id = (String) client.get("client_id");
                nickname = (String) client.get("nickname");
                phone = (String) client.get("phone");

            } else if ("LAWYER".equalsIgnoreCase(role)) {
                String lawyerSql = "SELECT lawyer_id FROM lawyer WHERE no = ?";
                Map<String, Object> lawyer = jdbcTemplate.queryForMap(lawyerSql, no);
                id = (String) lawyer.get("lawyer_id");
                // nickname 컬럼이 없으므로 그대로 빈 문자열 사용하거나
                // 필요하면 lawyer 이름 컬럼(예: name)으로 대체 조회
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("❌ 지원하지 않는 사용자 유형");
            }

//            System.out.println("🎯 사용자 정보: " + id + " / " + role + " / " + nickname);

            // 3. accessToken 재발급
            String newAccessToken = jwtTokenUtil.generateAccessToken(id, no, role, nickname,phone);
            System.out.println("재발급 완료: " + newAccessToken + '\n');

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 사용자 없음");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 토큰 재발급 실패");
        }
    }




    @PutMapping("/lawyer/info")
    public ResponseEntity<?> updateLawyerProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request
    ) {
        System.out.println("변호사 정보 수정 컨트롤러 진입");
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenUtil.parseToken(token); // JWT 파싱

        String lawyerId = claims.getSubject();
        String officeNumber = request.get("officeNumber");
        String phone = request.get("phone");
        String detailAddress = request.get("detailAddress");
        System.out.println(lawyerId);
        System.out.println(officeNumber);
        System.out.println(phone);
        System.out.println(detailAddress);



        if (officeNumber == null || phone == null || detailAddress == null ||
                officeNumber.trim().isEmpty() || phone.trim().isEmpty() || detailAddress.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("모든 필드를 입력해주세요.");
        }
        lawyerService.updateLawyerInfo(lawyerId, officeNumber, phone, detailAddress);
        return ResponseEntity.ok().build();
    }
    //관리자 전용 controller
    @PostMapping("/auth/admin/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String adminId = request.get("adminId");
        String password = request.get("password");

        AdminEntity admin = adminMapper.findByAdminId(adminId);
        if (admin == null || !passwordEncoder.matches(password, admin.getPwHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(adminId,admin.getNo(), "ADMIN", admin.getName(),admin.getPhone());
        String refreshToken = jwtTokenUtil.generateRefreshToken(adminId);

        refreshTokenService.save(admin.getNo(), refreshToken);

        Map<String, Object> res = new HashMap<>();
        res.put("accessToken", accessToken);
        res.put("refreshToken", refreshToken);
        res.put("name", admin.getName());

        return ResponseEntity.ok(res);
    }


}
