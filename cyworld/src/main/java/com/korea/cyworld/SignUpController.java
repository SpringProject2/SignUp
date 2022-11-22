package com.korea.cyworld;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import dao.SignUpDAO;
import util.Common;
import vo.SignUpVO;

@Controller
public class SignUpController {
	// SignUpDAO
	SignUpDAO signUp_dao;
	// SI방식
	public void setSignUp_dao(SignUpDAO signUp_dao) {
		this.signUp_dao = signUp_dao;
	}
	
	@RequestMapping(value= {"/", "/logout.do"})
	public String login() {
		return Common.PATH + "login.jsp";
	}
	
	@RequestMapping("/login_naver_callback.do")
	public String naver_join() {
		return Common.PATH + "login_naver_callback.jsp";
	}
	
	// 각 플랫폼별로 회원가입 페이지로 이동
	@RequestMapping("/login_authentication.do")
	public String login_authentication(SignUpVO vo, Model model) {
		// 플랫폼이 cyworld일때
		if ( vo.getPlatform().equals("cyworld") ) {
			model.addAttribute("vo", vo);
			return Common.PATH + "cyworld_join.jsp";
		}
		
		// 로그인이 소셜 로그인일때 (카카오 및 네이버)
		// 플랫폼별 가입자 조회 - vo.getPlatform + vo.getEmail
		SignUpVO joinVo = signUp_dao.selectOnePlatformEmail(vo);
		
		// 조회된 값이 없을때
		if ( joinVo == null ) {
			// 플랫폼이 카카오일때
			if ( vo.getPlatform().equals("kakao") ) {
				model.addAttribute("vo", vo);
				return Common.PATH + "kakao_join.jsp";
			}
			// 플랫폼이 네이버 일때
			if ( vo.getPlatform().equals("naver") ) {
				model.addAttribute("vo", vo);
				return Common.PATH + "naver_join.jsp";
			}
		}
		// 조회된 값이 있을때
		model.addAttribute("vo", joinVo); // DB에서 가져온 값을 바인딩
		return Common.PATH + "welcome.jsp"; // 환영 페이지로 포워딩
	}
	
	@RequestMapping("/double_check.do")
	@ResponseBody
	public String doubleCheck(String userID) {
		// ID 중복값 조회
		SignUpVO vo = signUp_dao.selectOneDoubleCheck(userID);
		
		// JSON형태로 콜백메소드에 전달할 결과값 생성
		String result = "{'result':'no'}"; // ID 중복일때
		if ( vo != null ) { // ID 중복이 아닐때
			result = "{'result':'yes'}";
		}
		// 콜백메소드에 결과값 전송
		return result;
	}
	
	// 로그인 체크
	@RequestMapping("/login_check.do")
	@ResponseBody
	public String loginCheck(SignUpVO vo) {
		
		String id = vo.getUserID(); // ID 입력값
		String pw = vo.getInfo(); // 비밀번호 입력값
		
		// ID값으로 고객 정보를 가져온다.
		SignUpVO loginCheckVo = signUp_dao.selectOneDoubleCheck(id);
		
		// 존재하지 않는 ID
		if ( loginCheckVo == null ) {
			return "{'result':'no_id'}";
		}
		
		// ID는 존재
		// 입력한 비밀번호와 DB에 ID에 해당하는 비밀번호가 불일치
		if ( !loginCheckVo.getInfo().equals(pw) ) {
			return "{'result':'no_info'}";
		}
		
		// 모두 일치
		// 로그인 가능
		return "{'result':'clear'}";
	}
	
	// 메인 페이지로 이동
	@RequestMapping("/main.do")
	public String welcome(SignUpVO vo, Model model) {
		// 넘어올때 ID값이 있을 경우 (플랫폼이 cyworld)
		if ( vo.getUserID() != null ) {
			// ID로 가입자인지 조회
			SignUpVO loginVo = signUp_dao.selectOneDoubleCheck(vo.getUserID());
			// 비가입자일때 (회원가입)
			if ( loginVo == null ) {
				// 아직 없는 정보값들 임의로 지정
				vo.setMinimi("");
				vo.setDotoryNum("");
				// 가입 성공시 고객 정보 저장
				signUp_dao.insertJoinSuccess(vo);
				model.addAttribute("vo", vo);
				return Common.PATH + "main.jsp";
			}
			// 가입자일때 (로그인)
			model.addAttribute("vo", loginVo);
			return Common.PATH + "main.jsp";
		}
		
		// 넘어올때 ID값이 없을 경우 (플랫폼이 소셜)
		// 아직 없는 정보값들 임의로 지정
		vo.setUserID("");
		vo.setInfo("");
		vo.setInfoR("");
		vo.setMinimi("");
		vo.setDotoryNum("");
		// 가입 성공시 고객 정보 저장
		signUp_dao.insertJoinSuccess(vo);
		model.addAttribute("vo", vo);
		return Common.PATH + "main.jsp";
	}
}
