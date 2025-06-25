package org.unilab.improfessorbe.domain.notice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unilab.improfessorbe.domain.notice.domain.Notice;
import org.unilab.improfessorbe.domain.notice.dto.request.NoticeRequest;
import org.unilab.improfessorbe.domain.notice.dto.response.NoticeResponse;
import org.unilab.improfessorbe.domain.notice.repository.NoticeRepository;
import org.unilab.improfessorbe.domain.user.domain.User;
import org.unilab.improfessorbe.domain.user.repository.UserRepository;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepository;
	private final UserRepository userRepository;

	@Transactional
	public void createNotice(Long userId, NoticeRequest noticeRequest) {
		validateAdminAccess(userId);
		Notice notice = NoticeRequest.toEntity(noticeRequest);
		noticeRepository.save(notice);
	}

	@Transactional
	public void updateNotice(Long noticeId, Long userId, NoticeRequest noticeRequest) {
		validateAdminAccess(userId);
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow( () -> new CustomException(ErrorCode.ELEMENT_NOT_FOUND));
		notice.updateNotice(noticeRequest.getTitle(), noticeRequest.getContent());
	}

	@Transactional(readOnly = true)
	public NoticeResponse getNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow( () -> new CustomException(ErrorCode.ELEMENT_NOT_FOUND));
		return NoticeResponse.of(notice);
	}

	@Transactional(readOnly = true)
	public List<NoticeResponse> getNotices() {
		List<Notice> notices = noticeRepository.findAll();
		List<NoticeResponse> noticesResponse = notices.stream().map(NoticeResponse::of).toList();
		return noticesResponse;
	}

	private void validateAdminAccess(Long userId) {
		Optional<User> user = userRepository.findByUserIdAndDeletedAtIsNull(userId);
		if(!user.isPresent()) throw new CustomException(ErrorCode.USER_NOT_FOUND);
		else{
			if(user.get().getRole()==User.Role.USER) throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
		}
	}




}
