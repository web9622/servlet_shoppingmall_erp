package com.company1.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.company1.DBManager;
import com.company1.dto.NoticeDTO;
import com.company1.dto.AttendanceDTO;
import com.company1.dto.CalendarDTO;
import com.company1.dto.MessageDTO;
import com.company1.dto.TodoDTO;

public class GroupwareDAO {

    // 공지사항 관련 메서드

    /**
     * 모든 공지사항을 조회합니다.
     */
    public List<NoticeDTO> getAllNotices() {
        List<NoticeDTO> notices = new ArrayList<>();
        String sql = "SELECT * FROM NOTICES WHERE IS_ACTIVE = 'Y' ORDER BY CREATE_DATE DESC";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NoticeDTO notice = new NoticeDTO();
                notice.setNoticeId(rs.getInt("NOTICE_ID"));
                notice.setTitle(rs.getString("TITLE"));
                notice.setContent(rs.getString("CONTENT"));
                notice.setAuthorId(rs.getString("AUTHOR_ID"));
                notice.setAuthorName(rs.getString("AUTHOR_NAME"));
                notice.setCreateDate(rs.getDate("CREATE_DATE"));
                notice.setUpdateDate(rs.getDate("UPDATE_DATE"));
                notice.setViewCount(rs.getInt("VIEW_COUNT"));
                notice.setIsActive(rs.getString("IS_ACTIVE"));
                notices.add(notice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notices;
    }

    /**
     * 새로운 공지사항을 등록합니다.
     */
    public boolean insertNotice(NoticeDTO notice) {
        String sql = "INSERT INTO NOTICES (NOTICE_ID, TITLE, CONTENT, AUTHOR_ID, AUTHOR_NAME) " +
                "VALUES (nextval('SEQ_NOTICE_ID'), ?, ?, ?, ?)";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getContent());
            pstmt.setString(3, notice.getAuthorId());
            pstmt.setString(4, notice.getAuthorName());

            // 실행 전에 값들 로깅
            System.out.println("Notice Insert 시도: authorId=" + notice.getAuthorId() +
                    ", authorName=" + notice.getAuthorName() +
                    ", title=" + notice.getTitle());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            // FK 위반 에러인 경우 (ORA-02291)
            if (e.getErrorCode() == 2291) {
                System.err.println("FK 제약조건 위반: AUTHOR_ID(" + notice.getAuthorId() +
                        ")가 EMPLOYEES 테이블에 존재하지 않습니다.");
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 공지사항 조회수를 증가시킵니다.
     */
    public void increaseViewCount(int noticeId) {
        String sql = "UPDATE NOTICES SET VIEW_COUNT = VIEW_COUNT + 1 WHERE NOTICE_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noticeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 출퇴근 관리 관련 메서드

    /**
     * 출근 처리를 합니다.
     */
    public boolean checkIn(String userId) {
        String sql = "INSERT INTO ATTENDANCE (ATTENDANCE_ID, USER_ID, WORK_DATE, CHECK_IN_TIME) " +
                "VALUES (nextval('SEQ_ATTENDANCE_ID'), ?, CURRENT_DATE, CURRENT_TIMESTAMP)";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 퇴근 처리를 합니다.
     */
    public boolean checkOut(String userId) {
        // 1) 아직 퇴근전인 오늘 레코드에서 출근시간 가져오기
        final String selectSql = "SELECT CHECK_IN_TIME FROM ATTENDANCE " +
                "WHERE USER_ID = ? AND WORK_DATE = CURRENT_DATE AND CHECK_OUT_TIME IS NULL";

        // 2) 근무시간/상태 포함해 갱신
        final String updateSql = "UPDATE ATTENDANCE SET " +
                "  CHECK_OUT_TIME = CURRENT_TIMESTAMP, " +
                "  TOTAL_WORK_MINUTES = ?, " +
                "  STATUS = CASE " +
                "    WHEN ? < 480 THEN 'EARLY_LEAVE' " + // 8시간 미만
                "    WHEN ? > 600 THEN 'OVERTIME' " + // 10시간 초과
                "    ELSE 'PRESENT' " + // 정상 근무
                "  END " +
                "WHERE USER_ID = ? AND WORK_DATE = CURRENT_DATE AND CHECK_OUT_TIME IS NULL";

        try (Connection conn = DBManager.getDBConnection()) {
            // (선택) 자동 커밋 해제하고 트랜잭션으로 묶고 싶으면 주석 해제
            // conn.setAutoCommit(false);

            Timestamp checkInTime = null;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        checkInTime = rs.getTimestamp("CHECK_IN_TIME");
                    } else {
                        // 오늘 출근 기록이 없거나 이미 퇴근 처리됨
                        return false;
                    }
                }
            }

            if (checkInTime == null) {
                return false;
            }

            // 근무시간(분) 계산
            long nowMillis = System.currentTimeMillis();
            long inMillis = checkInTime.getTime();
            int workMinutes = (int) ((nowMillis - inMillis) / (1000 * 60));

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, workMinutes);
                ps.setInt(2, workMinutes);
                ps.setInt(3, workMinutes);
                ps.setString(4, userId);
                int updated = ps.executeUpdate();

                // (선택) 트랜잭션 사용할 때 커밋
                // conn.commit();

                return updated > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // (선택) 트랜잭션 사용 시 롤백
            // try { conn.rollback(); } catch (SQLException ignore) {}
            return false;
        }
    }

    /**
     * 특정 사용자의 오늘 출퇴근 정보를 조회합니다.
     */
    public AttendanceDTO getTodayAttendance(String userId) {
        String sql = "SELECT * FROM ATTENDANCE WHERE USER_ID = ? AND WORK_DATE = CURRENT_DATE";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                AttendanceDTO attendance = new AttendanceDTO();
                attendance.setAttendanceId(rs.getInt("ATTENDANCE_ID"));
                attendance.setUserId(rs.getString("USER_ID"));
                attendance.setWorkDate(rs.getDate("WORK_DATE"));
                attendance.setCheckInTime(rs.getTimestamp("CHECK_IN_TIME"));
                attendance.setCheckOutTime(rs.getTimestamp("CHECK_OUT_TIME"));
                attendance.setTotalWorkMinutes(rs.getInt("TOTAL_WORK_MINUTES"));
                attendance.setBreakMinutes(rs.getInt("BREAK_MINUTES"));
                attendance.setOvertimeMinutes(rs.getInt("OVERTIME_MINUTES"));
                attendance.setStatus(rs.getString("STATUS"));
                attendance.setNotes(rs.getString("NOTES"));
                attendance.setCreateDate(rs.getDate("CREATE_DATE"));
                attendance.setUpdateDate(rs.getDate("UPDATE_DATE"));
                return attendance;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 오늘 이미 출근했는지 확인합니다.
     */
    public boolean isAlreadyCheckedIn(String userId) {
        String sql = "SELECT COUNT(*) FROM ATTENDANCE WHERE USER_ID = ? AND WORK_DATE = CURRENT_DATE AND CHECK_IN_TIME IS NOT NULL";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 오늘 이미 퇴근했는지 확인합니다.
     */
    public boolean isAlreadyCheckedOut(String userId) {
        String sql = "SELECT COUNT(*) FROM ATTENDANCE WHERE USER_ID = ? AND WORK_DATE = CURRENT_DATE AND CHECK_OUT_TIME IS NOT NULL";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 사용자의 월별 출퇴근 통계를 조회합니다.
     */
    public List<AttendanceDTO> getMonthlyAttendance(String userId, String yearMonth) {
        List<AttendanceDTO> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM ATTENDANCE WHERE USER_ID = ? AND TO_CHAR(WORK_DATE, 'YYYY-MM') = ? ORDER BY WORK_DATE";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                AttendanceDTO attendance = new AttendanceDTO();
                attendance.setAttendanceId(rs.getInt("ATTENDANCE_ID"));
                attendance.setUserId(rs.getString("USER_ID"));
                attendance.setWorkDate(rs.getDate("WORK_DATE"));
                attendance.setCheckInTime(rs.getTimestamp("CHECK_IN_TIME"));
                attendance.setCheckOutTime(rs.getTimestamp("CHECK_OUT_TIME"));
                attendance.setTotalWorkMinutes(rs.getInt("TOTAL_WORK_MINUTES"));
                attendance.setBreakMinutes(rs.getInt("BREAK_MINUTES"));
                attendance.setOvertimeMinutes(rs.getInt("OVERTIME_MINUTES"));
                attendance.setStatus(rs.getString("STATUS"));
                attendance.setNotes(rs.getString("NOTES"));
                attendance.setCreateDate(rs.getDate("CREATE_DATE"));
                attendance.setUpdateDate(rs.getDate("UPDATE_DATE"));
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    /**
     * 공지사항을 수정합니다.
     */
    public boolean updateNotice(NoticeDTO notice) {
        String sql = "UPDATE NOTICES SET TITLE = ?, CONTENT = ?, UPDATE_DATE = SYSDATE " +
                "WHERE NOTICE_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getContent());
            pstmt.setInt(3, notice.getNoticeId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 공지사항을 삭제합니다 (논리적 삭제).
     */
    public boolean deleteNotice(int noticeId) {
        String sql = "UPDATE NOTICES SET IS_ACTIVE = 'N' WHERE NOTICE_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noticeId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 특정 공지사항을 조회합니다.
     */
    public NoticeDTO getNoticeById(int noticeId) {
        String sql = "SELECT * FROM NOTICES WHERE NOTICE_ID = ? AND IS_ACTIVE = 'Y'";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noticeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                NoticeDTO notice = new NoticeDTO();
                notice.setNoticeId(rs.getInt("NOTICE_ID"));
                notice.setTitle(rs.getString("TITLE"));
                notice.setContent(rs.getString("CONTENT"));
                notice.setAuthorId(rs.getString("AUTHOR_ID"));
                notice.setAuthorName(rs.getString("AUTHOR_NAME"));
                notice.setCreateDate(rs.getDate("CREATE_DATE"));
                notice.setUpdateDate(rs.getDate("UPDATE_DATE"));
                notice.setViewCount(rs.getInt("VIEW_COUNT"));
                notice.setIsActive(rs.getString("IS_ACTIVE"));
                return notice;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== 캘린더 관련 메서드 =====

    /**
     * 특정 월의 사용자 일정을 조회합니다.
     */
    public List<CalendarDTO> getMonthlyEvents(String userId, String yearMonth) {
        List<CalendarDTO> events = new ArrayList<>();
        String sql = "SELECT * FROM CALENDAR_EVENTS WHERE USER_ID = ? " +
                "AND TO_CHAR(START_DATE, 'YYYY-MM') = ? " +
                "ORDER BY START_DATE";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CalendarDTO event = new CalendarDTO();
                event.setEventId(rs.getInt("EVENT_ID"));
                event.setUserId(rs.getString("USER_ID"));
                event.setTitle(rs.getString("TITLE"));
                event.setDescription(rs.getString("DESCRIPTION"));
                event.setEventType(rs.getString("EVENT_TYPE"));
                event.setStartDate(rs.getTimestamp("START_DATE"));
                event.setEndDate(rs.getTimestamp("END_DATE"));
                event.setIsAllDay(rs.getString("IS_ALL_DAY"));
                event.setLocation(rs.getString("LOCATION"));
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     * 새로운 일정을 추가합니다.
     */
    public boolean addEvent(CalendarDTO event) {
        String sql = "INSERT INTO CALENDAR_EVENTS (EVENT_ID, USER_ID, TITLE, DESCRIPTION, " +
                "EVENT_TYPE, START_DATE, END_DATE, IS_ALL_DAY, LOCATION) " +
                "VALUES (nextval('SEQ_EVENT_ID'), ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getUserId());
            pstmt.setString(2, event.getTitle());
            pstmt.setString(3, event.getDescription());
            pstmt.setString(4, event.getEventType());
            pstmt.setTimestamp(5, event.getStartDate());
            pstmt.setTimestamp(6, event.getEndDate());
            pstmt.setString(7, event.getIsAllDay());
            pstmt.setString(8, event.getLocation());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 일정을 수정합니다.
     */
    public boolean updateEvent(CalendarDTO event) {
        String sql = "UPDATE CALENDAR_EVENTS SET TITLE = ?, DESCRIPTION = ?, " +
                "EVENT_TYPE = ?, START_DATE = ?, END_DATE = ?, IS_ALL_DAY = ?, " +
                "LOCATION = ? WHERE EVENT_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getDescription());
            pstmt.setString(3, event.getEventType());
            pstmt.setTimestamp(4, event.getStartDate());
            pstmt.setTimestamp(5, event.getEndDate());
            pstmt.setString(6, event.getIsAllDay());
            pstmt.setString(7, event.getLocation());
            pstmt.setInt(8, event.getEventId());
            pstmt.setString(9, event.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 일정을 삭제합니다.
     */
    public boolean deleteEvent(int eventId, String userId) {
        String sql = "DELETE FROM CALENDAR_EVENTS WHERE EVENT_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setString(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 특정 일정을 ID로 조회합니다.
     */
    public CalendarDTO getEventById(int eventId, String userId) {
        String sql = "SELECT * FROM CALENDAR_EVENTS WHERE EVENT_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eventId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                CalendarDTO event = new CalendarDTO();
                event.setEventId(rs.getInt("EVENT_ID"));
                event.setUserId(rs.getString("USER_ID"));
                event.setTitle(rs.getString("TITLE"));
                event.setDescription(rs.getString("DESCRIPTION"));
                event.setEventType(rs.getString("EVENT_TYPE"));
                event.setStartDate(rs.getTimestamp("START_DATE"));
                event.setEndDate(rs.getTimestamp("END_DATE"));
                event.setIsAllDay(rs.getString("IS_ALL_DAY"));
                event.setLocation(rs.getString("LOCATION"));
                return event;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== 메시지 관련 메서드 =====

    /**
     * 새로운 메시지를 전송합니다.
     */
    public boolean sendMessage(MessageDTO message) {
        String sql = "INSERT INTO MESSAGES (MESSAGE_ID, SENDER_ID, SENDER_NAME, RECEIVER_ID, " +
                "RECEIVER_NAME, MESSAGE_TYPE, CONTENT, IS_READ, SEND_DATE) " +
                "VALUES (nextval('SEQ_MESSAGE_ID'), ?, ?, ?, ?, ?, ?, 'N', CURRENT_TIMESTAMP)";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message.getSenderId());
            pstmt.setString(2, message.getSenderName());
            pstmt.setString(3, message.getReceiverId());
            pstmt.setString(4, message.getReceiverName());
            pstmt.setString(5, message.getMessageType());
            pstmt.setString(6, message.getContent());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 사용자가 받은 메시지 목록을 조회합니다.
     */
    public List<MessageDTO> getReceivedMessages(String userId, int limit) {
        List<MessageDTO> messages = new ArrayList<>();
        String sql = "SELECT * FROM MESSAGES " +
                "WHERE RECEIVER_ID = ? OR RECEIVER_ID IS NULL " +
                "ORDER BY SEND_DATE DESC LIMIT ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MessageDTO message = new MessageDTO();
                message.setMessageId(rs.getInt("MESSAGE_ID"));
                message.setSenderId(rs.getString("SENDER_ID"));
                message.setSenderName(rs.getString("SENDER_NAME"));
                message.setReceiverId(rs.getString("RECEIVER_ID"));
                message.setReceiverName(rs.getString("RECEIVER_NAME"));
                message.setMessageType(rs.getString("MESSAGE_TYPE"));
                message.setContent(rs.getString("CONTENT"));
                message.setIsRead(rs.getString("IS_READ"));
                message.setSendDate(rs.getTimestamp("SEND_DATE"));
                message.setReadDate(rs.getTimestamp("READ_date"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 사용자가 보낸 메시지 목록을 조회합니다.
     */
    public List<MessageDTO> getSentMessages(String userId, int limit) {
        List<MessageDTO> messages = new ArrayList<>();
        String sql = "SELECT * FROM MESSAGES " +
                "WHERE SENDER_ID = ? " +
                "ORDER BY SEND_DATE DESC LIMIT ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MessageDTO message = new MessageDTO();
                message.setMessageId(rs.getInt("MESSAGE_ID"));
                message.setSenderId(rs.getString("SENDER_ID"));
                message.setSenderName(rs.getString("SENDER_NAME"));
                message.setReceiverId(rs.getString("RECEIVER_ID"));
                message.setReceiverName(rs.getString("RECEIVER_NAME"));
                message.setMessageType(rs.getString("MESSAGE_TYPE"));
                message.setContent(rs.getString("CONTENT"));
                message.setIsRead(rs.getString("IS_READ"));
                message.setSendDate(rs.getTimestamp("SEND_DATE"));
                message.setReadDate(rs.getTimestamp("read_date"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 특정 사용자 간의 대화 내역을 조회합니다.
     */
    public List<MessageDTO> getConversation(String userId1, String userId2, int limit) {
        List<MessageDTO> messages = new ArrayList<>();
        String sql = "SELECT * FROM (" +
                "  SELECT * FROM MESSAGES " +
                "  WHERE (SENDER_ID = ? AND RECEIVER_ID = ?) " +
                "     OR (SENDER_ID = ? AND RECEIVER_ID = ?) " +
                "  ORDER BY SEND_DATE DESC" +
                ") WHERE ROWNUM <= ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId1);
            pstmt.setString(2, userId2);
            pstmt.setString(3, userId2);
            pstmt.setString(4, userId1);
            pstmt.setInt(5, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MessageDTO message = new MessageDTO();
                message.setMessageId(rs.getInt("MESSAGE_ID"));
                message.setSenderId(rs.getString("SENDER_ID"));
                message.setSenderName(rs.getString("SENDER_NAME"));
                message.setReceiverId(rs.getString("RECEIVER_ID"));
                message.setReceiverName(rs.getString("RECEIVER_NAME"));
                message.setMessageType(rs.getString("MESSAGE_TYPE"));
                message.setContent(rs.getString("CONTENT"));
                message.setIsRead(rs.getString("IS_READ"));
                message.setSendDate(rs.getTimestamp("SEND_DATE"));
                message.setReadDate(rs.getTimestamp("read_date"));
                messages.add(message);
            }

            // 시간순으로 정렬 (오래된 것부터)
            messages.sort((m1, m2) -> m1.getSendDate().compareTo(m2.getSendDate()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * 사용자의 메시지 목록을 조회합니다.
     * 수신한 메시지와 발신한 메시지를 모두 포함합니다.
     */
    public List<MessageDTO> getUserMessages(String userId) {
        List<MessageDTO> messages = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT M.*, " +
                "S.EMP_NAME AS SENDER_NAME, " +
                "R.EMP_NAME AS RECEIVER_NAME " +
                "FROM MESSAGES M " +
                "LEFT JOIN EMPLOYEES S ON M.SENDER_ID = S.EMP_ID " +
                "LEFT JOIN EMPLOYEES R ON M.RECEIVER_ID = R.EMP_ID " +
                "WHERE M.SENDER_ID = ? OR M.RECEIVER_ID = ? OR M.MESSAGE_TYPE = 'BROADCAST' " +
                "ORDER BY M.SEND_DATE DESC";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                MessageDTO msg = new MessageDTO();
                msg.setMessageId(rs.getInt("MESSAGE_ID"));
                msg.setSenderId(rs.getString("SENDER_ID"));
                msg.setSenderName(rs.getString("SENDER_NAME"));
                msg.setReceiverId(rs.getString("RECEIVER_ID"));
                msg.setReceiverName(rs.getString("RECEIVER_NAME"));
                msg.setMessageType(rs.getString("MESSAGE_TYPE"));
                msg.setContent(rs.getString("CONTENT"));
                msg.setIsRead(rs.getString("IS_READ"));
                msg.setSendDate(rs.getTimestamp("SEND_DATE"));

                Timestamp readDate = rs.getTimestamp("READ_DATE");
                if (readDate != null) {
                    msg.setReadDate(readDate);
                }

                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(rs, pstmt, conn);
        }

        return messages;
    }

    /**
     * 메시지를 삭제합니다.
     */
    public boolean deleteMessage(int messageId, String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        String sql = "DELETE FROM MESSAGES WHERE MESSAGE_ID = ? AND (SENDER_ID = ? OR RECEIVER_ID = ?)";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);
            pstmt.setString(2, userId);
            pstmt.setString(3, userId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }

        return result > 0;
    }

    /**
     * 메시지를 읽음 표시합니다.
     */
    public boolean markMessageAsRead(int messageId, String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        String sql = "UPDATE MESSAGES SET IS_READ = 'Y', READ_DATE = CURRENT_TIMESTAMP " +
                "WHERE MESSAGE_ID = ? AND RECEIVER_ID = ? AND IS_READ = 'N'";

        try {
            conn = DBManager.getDBConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);
            pstmt.setString(2, userId);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(null, pstmt, conn);
        }

        return result > 0;
    }

    /**
     * 사용자의 읽지 않은 메시지 개수를 조회합니다.
     */
    public int getUnreadMessageCount(String userId) {
        String sql = "SELECT COUNT(*) FROM MESSAGES " +
                "WHERE RECEIVER_ID = ? AND IS_READ = 'N'";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 브로드캐스트 메시지를 전송합니다 (전체 공지용).
     */
    public boolean sendBroadcastMessage(MessageDTO message) {
        String sql = "INSERT INTO MESSAGES (MESSAGE_ID, SENDER_ID, SENDER_NAME, " +
                "MESSAGE_TYPE, CONTENT, IS_READ, SEND_DATE) " +
                "VALUES (SEQ_MESSAGE_ID.NEXTVAL, ?, ?, 'BROADCAST', ?, 'N', CURRENT_TIMESTAMP)";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message.getSenderId());
            pstmt.setString(2, message.getSenderName());
            pstmt.setString(3, message.getContent());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 특정 메시지를 ID로 조회합니다.
     */
    public MessageDTO getMessageById(int messageId) {
        String sql = "SELECT * FROM MESSAGES WHERE MESSAGE_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                MessageDTO message = new MessageDTO();
                message.setMessageId(rs.getInt("MESSAGE_ID"));
                message.setSenderId(rs.getString("SENDER_ID"));
                message.setSenderName(rs.getString("SENDER_NAME"));
                message.setReceiverId(rs.getString("RECEIVER_ID"));
                message.setReceiverName(rs.getString("RECEIVER_NAME"));
                message.setMessageType(rs.getString("MESSAGE_TYPE"));
                message.setContent(rs.getString("CONTENT"));
                message.setIsRead(rs.getString("IS_READ"));
                message.setSendDate(rs.getTimestamp("SEND_DATE"));
                message.setReadDate(rs.getTimestamp("read_date"));
                return message;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== 할일 관리 관련 메서드 =====

    /**
     * 특정 사용자의 할일 목록을 조회합니다.
     */
    public List<TodoDTO> getUserTodos(String userId) {
        List<TodoDTO> todos = new ArrayList<>();
        String sql = "SELECT * FROM TODOS WHERE USER_ID = ? ORDER BY CREATE_DATE DESC";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TodoDTO todo = new TodoDTO();
                todo.setTodoId(rs.getInt("TODO_ID"));
                todo.setUserId(rs.getString("USER_ID"));
                todo.setTitle(rs.getString("TITLE"));
                todo.setDescription(rs.getString("DESCRIPTION"));
                todo.setIsCompleted(rs.getString("IS_COMPLETED"));
                todo.setPriority(rs.getInt("PRIORITY"));
                todo.setDueDate(rs.getDate("DUE_DATE"));
                todo.setCreateDate(rs.getDate("CREATE_DATE"));
                todo.setCompleteDate(rs.getDate("COMPLETE_DATE"));
                todos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todos;
    }

    /**
     * 새로운 할일을 추가합니다.
     */
    public boolean addTodo(TodoDTO todo) {
        String sql = "INSERT INTO TODOS (TODO_ID, USER_ID, TITLE, DESCRIPTION, PRIORITY, DUE_DATE) " +
                "VALUES (nextval('SEQ_TODO_ID'), ?, ?, ?, ?, ?)";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getUserId());
            pstmt.setString(2, todo.getTitle());
            pstmt.setString(3, todo.getDescription());
            pstmt.setInt(4, todo.getPriority());
            pstmt.setDate(5, todo.getDueDate());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 할일을 수정합니다.
     */
    public boolean updateTodo(TodoDTO todo) {
        String sql = "UPDATE TODOS SET TITLE = ?, DESCRIPTION = ?, PRIORITY = ?, DUE_DATE = ? " +
                "WHERE TODO_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitle());
            pstmt.setString(2, todo.getDescription());
            pstmt.setInt(3, todo.getPriority());
            pstmt.setDate(4, todo.getDueDate());
            pstmt.setInt(5, todo.getTodoId());
            pstmt.setString(6, todo.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 할일을 삭제합니다.
     */
    public boolean deleteTodo(int todoId, String userId) {
        String sql = "DELETE FROM TODOS WHERE TODO_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, todoId);
            pstmt.setString(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 할일의 완료/미완료 상태를 토글합니다.
     */
    public boolean toggleTodoCompletion(int todoId, String userId) {
        String sql = "UPDATE TODOS SET " +
                "IS_COMPLETED = CASE WHEN IS_COMPLETED = 'Y' THEN 'N' ELSE 'Y' END, " +
                "COMPLETE_DATE = CASE WHEN IS_COMPLETED = 'Y' THEN NULL ELSE CURRENT_TIMESTAMP END " +
                "WHERE TODO_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, todoId);
            pstmt.setString(2, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 특정 할일을 ID로 조회합니다.
     */
    public TodoDTO getTodoById(int todoId, String userId) {
        String sql = "SELECT * FROM TODOS WHERE TODO_ID = ? AND USER_ID = ?";

        try (Connection conn = DBManager.getDBConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, todoId);
            pstmt.setString(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                TodoDTO todo = new TodoDTO();
                todo.setTodoId(rs.getInt("TODO_ID"));
                todo.setUserId(rs.getString("USER_ID"));
                todo.setTitle(rs.getString("TITLE"));
                todo.setDescription(rs.getString("DESCRIPTION"));
                todo.setIsCompleted(rs.getString("IS_COMPLETED"));
                todo.setPriority(rs.getInt("PRIORITY"));
                todo.setDueDate(rs.getDate("DUE_DATE"));
                todo.setCreateDate(rs.getDate("CREATE_DATE"));
                todo.setCompleteDate(rs.getDate("COMPLETE_DATE"));
                return todo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
