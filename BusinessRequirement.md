# BUSINESS REQUIREMENTS DOCUMENT (BRD)

## Project: Snake Game Application

---

# 1. Giới thiệu đề tài Snake Game

## 1.1 Giới thiệu hệ thống

Snake Game (Rắn săn mồi) là một trò chơi điện tử quen thuộc, trong đó người chơi điều khiển rắn di chuyển trong bàn chơi dạng lưới để ăn thức ăn. Mỗi lần ăn thức ăn thành công, rắn sẽ dài ra và điểm số của người chơi tăng lên.

Trong quá trình chơi, độ khó của trò chơi có thể tăng dần thông qua tốc độ di chuyển của rắn hoặc cách xuất hiện của thức ăn, nhằm tạo thêm thử thách cho người chơi. Trò chơi kết thúc khi rắn va chạm với biên hoặc tự va chạm vào chính nó.

Hệ thống được xây dựng nhằm mang lại trải nghiệm giải trí đơn giản, dễ tiếp cận nhưng vẫn đảm bảo tính tương tác và tính thử thách. Đồng thời, hệ thống cũng đóng vai trò như một bài toán nền tảng phục vụ mục đích học tập, hỗ trợ phân tích yêu cầu và phát triển phần mềm.

Ứng dụng được triển khai dưới dạng phần mềm desktop, phù hợp cho môi trường học tập và thực hành phát triển hệ thống.

---


## 1.2 Phạm vi khái quát

Hệ thống tập trung vào chế độ chơi đơn với các chức năng chính bao gồm điều khiển rắn, cập nhật trạng thái trò chơi, xử lý va chạm, tính điểm, lưu điểm cao, tạm dừng trò chơi và lựa chọn mức độ khó.

Hệ thống không bao gồm các chức năng như chơi nhiều người, kết nối mạng hoặc tích hợp với các dịch vụ bên ngoài.

---

## 1.3 Luồng hoạt động tổng quát của hệ thống

Người dùng bắt đầu trò chơi từ màn hình chính và lựa chọn mức độ khó trước khi bắt đầu. Sau khi trò chơi được khởi động, người chơi điều khiển rắn di chuyển trong bàn chơi để ăn thức ăn và tích lũy điểm số.

Trong quá trình chơi, người chơi có thể tạm dừng hoặc tiếp tục trò chơi tùy theo nhu cầu.

Trò chơi kết thúc khi xảy ra va chạm với biên hoặc với chính thân rắn. Sau khi kết thúc, người chơi có thể bắt đầu lại trò chơi. Hệ thống sẽ ghi nhận và cập nhật điểm cao nếu người chơi đạt kết quả tốt hơn trước đó.


# 2. Lý do chọn đề tài

Trò chơi Snake Game được lựa chọn vì có luật chơi đơn giản nhưng vẫn thể hiện đầy đủ các thành phần cơ bản của một hệ thống phần mềm tương tác.

Đề tài này phù hợp cho mục đích học tập vì có thể mô phỏng nhiều chức năng quan trọng trong phát triển phần mềm như:

- Xử lý thao tác và tương tác từ người dùng  
- Cập nhật trạng thái hệ thống theo thời gian  
- Quản lý dữ liệu thay đổi liên tục như vị trí rắn, thức ăn và điểm số  
- Dễ dàng mở rộng thêm các tính năng như tăng độ khó hoặc chế độ chơi khác nhau  
- Phù hợp để xây dựng một hệ thống rõ ràng, dễ hiểu và dễ bảo trì  

---


# 3. Mục tiêu hệ thống

## 3.1 Mục tiêu tổng quát

Xây dựng một hệ thống Snake Game hoạt động ổn định, phản hồi nhanh và mang lại trải nghiệm chơi mượt mà cho người dùng.

## 3.2 Mục tiêu cụ thể

- Hệ thống phản hồi nhanh khi người dùng điều khiển.
- Trạng thái trò chơi được cập nhật liên tục và chính xác.
- Dữ liệu như vị trí, điểm số, trạng thái được quản lý nhất quán.
- Giao diện hiển thị đúng với trạng thái hiện tại của trò chơi.
- Hỗ trợ lưu và hiển thị điểm cao.

---

# 4. Stakeholders

- Người chơi (End Users): Sử dụng hệ thống để giải trí.
- Nhóm phát triển (Development Team): Xây dựng và duy trì hệ thống.
- Tester/QA: Kiểm tra chức năng và đảm bảo hệ thống hoạt động đúng theo các yêu cầu đã xác định.
---

# 5. Phạm vi hệ thống

## 5.1 In-Scope

Hệ thống Snake Game hỗ trợ chế độ chơi đơn, trong đó người dùng điều khiển rắn trong môi trường lưới để ăn thức ăn, tăng điểm và tránh va chạm. Các chức năng nằm trong phạm vi hệ thống bao gồm:

### 5.1.1 Khởi tạo và cấu hình trò chơi
- Bắt đầu trò chơi và khởi tạo trạng thái ban đầu.
- Chọn mức độ khó trước khi bắt đầu trò chơi.

### 5.1.2 Hiển thị và trạng thái hệ thống
- Hiển thị trạng thái trò chơi như đang chơi, tạm dừng và kết thúc.

### 5.1.3 Điều khiển và cập nhật gameplay
- Điều khiển hướng di chuyển của rắn.
- Cập nhật trạng thái theo thời gian.
- Quản lý vị trí của các thành phần trong game.

### 5.1.4 Xử lý logic trò chơi
- Xử lý các sự kiện như ăn thức ăn và va chạm.
- Tính toán và hiển thị điểm số.

### 5.1.5 Điều khiển phiên chơi
- Tạm dừng và tiếp tục trò chơi.
- Lưu trữ và hiển thị điểm cao.

---

## 5.2 Out-of-Scope

Các chức năng sau không thuộc phạm vi của hệ thống trong phiên bản hiện tại:

- Chế độ nhiều người chơi.
- Kết nối mạng hoặc hệ thống server.
- Các chức năng thanh toán.
- Tích hợp mạng xã hội.
- Chọn mức độ khó nâng cao (dành cho các giai đoạn phát triển sau).

# 6. Yêu cầu người dùng (User Requirements)

- UR01: Người dùng có thể bắt đầu trò chơi từ màn hình chính.
- UR02: Người dùng có thể chọn mức độ khó trước khi bắt đầu trò chơi.
- UR03: Người dùng có thể điều khiển hướng di chuyển của rắn.
- UR04: Hệ thống phản hồi ngay khi người dùng thao tác.
- UR05: Người dùng có thể xem điểm số trong quá trình chơi.
- UR06: Người dùng có thể tạm dừng và tiếp tục trò chơi trong quá trình chơi.
- UR07: Người dùng được thông báo khi trò chơi kết thúc.
- UR08: Người dùng có thể chơi lại sau khi kết thúc.
- UR09: Người dùng có thể xem lại điểm cao nhất.

---

# 7. Yêu cầu chức năng (Functional Requirements)

## FR01: Khởi tạo trò chơi
- Hệ thống cho phép người dùng bắt đầu trò chơi từ màn hình chính.
- Hệ thống shall khởi tạo rắn, thức ăn ban đầu, điểm số và trạng thái trò chơi.
- Sau khi khởi tạo, trạng thái trò chơi chuyển sang trạng thái “đang chơi”.

---

## FR02: Chọn mức độ khó
- Hệ thống cho phép người dùng chọn mức độ khó trước khi bắt đầu trò chơi.
- Mức độ khó ảnh hưởng đến tốc độ di chuyển của rắn.
- Nếu không được chọn, hệ thống sử dụng mức độ khó mặc định.

---

## FR03: Điều khiển rắn
- Hệ thống cho phép người dùng thay đổi hướng di chuyển của rắn thông qua bàn phím.
- Hệ thống không cho phép rắn thay đổi sang hướng ngược lại ngay lập tức (180°).

---

## FR04: Di chuyển rắn
- Hệ thống đảm bảo rắn di chuyển liên tục theo hướng hiện tại.
- Hệ thống cập nhật chính xác vị trí của rắn sau mỗi bước di chuyển.

---

## FR05: Cập nhật trạng thái trò chơi
- Hệ thống shall cập nhật trạng thái trò chơi theo chu kỳ thời gian liên tục.
- Việc cập nhật bao gồm vị trí rắn, trạng thái game và các sự kiện trong trò chơi.

---

## FR06: Sinh thức ăn
- Hệ thống tạo thức ăn tại các vị trí hợp lệ trong lưới trò chơi.
- Thức ăn không được phép xuất hiện trùng với vị trí của rắn.

---

## FR07: Phát hiện sự kiện
- Hệ thống phát hiện khi rắn ăn thức ăn.
- Hệ thống phát hiện khi rắn va chạm với biên của bàn chơi.
- Hệ thống phát hiện khi rắn va chạm với chính thân rắn.

---

## FR08: Xử lý ăn thức ăn
- Khi rắn ăn thức ăn, hệ thống tăng chiều dài của rắn.
- Hệ thống cập nhật điểm số tương ứng.
- Hệ thống sinh lại thức ăn mới tại vị trí hợp lệ.

---

## FR09: Quản lý điểm số
- Hệ thống tính toán và cập nhật điểm số trong suốt quá trình chơi.
- Điểm số được hiển thị theo thời gian thực.

---

## FR10: Lưu điểm cao
- Hệ thống lưu trữ điểm cao nhất đạt được.
- Dữ liệu điểm cao được lưu trữ cục bộ trên thiết bị người dùng.
- Điểm cao được giữ lại giữa các lần chơi.

---

## FR11: Hiển thị trò chơi
- Hệ thống hiển thị đầy đủ rắn, thức ăn và điểm số.
- Giao diện phản ánh chính xác trạng thái trò chơi tại mọi thời điểm.

---

## FR12: Tạm dừng và tiếp tục
- Hệ thống cho phép người dùng tạm dừng trò chơi.
- Khi tạm dừng, trạng thái trò chơi không được cập nhật và rắn dừng di chuyển.
- Người dùng có thể tiếp tục trò chơi từ trạng thái đã tạm dừng.

---

## FR13: Kết thúc và chơi lại
- Hệ thống kết thúc trò chơi khi xảy ra va chạm.
- Hệ thống hiển thị trạng thái kết thúc cùng điểm số đạt được.
- Hệ thống cho phép người dùng bắt đầu lại trò chơi từ trạng thái ban đầu.

---

# 7.1 Acceptance Criteria

- Khi người chơi nhập hướng hợp lệ, rắn phải đổi hướng tương ứng.
- Khi người chơi nhập hướng ngược 180°, rắn phải giữ nguyên hướng hiện tại.
- Khi rắn ăn thức ăn, điểm số phải tăng và rắn phải dài ra.
- Khi rắn ăn thức ăn, hệ thống phải sinh thức ăn mới tại vị trí hợp lệ.
- Khi rắn va chạm biên hoặc thân, trò chơi phải chuyển sang trạng thái kết thúc.
- Khi người chơi chọn chơi lại, hệ thống phải khởi tạo lại trạng thái ban đầu.
- Khi người chơi tạm dừng, rắn phải ngừng di chuyển.
- Khi bắt đầu trò chơi, điểm số phải được khởi tạo về 0 và trạng thái game được reset.
- Khi thay đổi mức độ khó, tốc độ rắn phải thay đổi tương ứng.
- Khi đạt điểm cao hơn hiện tại, hệ thống phải cập nhật và lưu điểm cao mới.
---

## 7.3 Requirement Mapping (UR ↔ FR)

| User Requirement | Functional Requirement liên quan |
|---|---|
| UR01 | FR01 |
| UR02 | FR02 |
| UR03 | FR03 |
| UR04 | FR05 |
| UR05 | FR09, FR11 |
| UR06 | FR12 |
| UR07 | FR13 |
| UR08 | FR13 |
| UR09 | FR10 |

---

# 8. Yêu cầu phi chức năng (Non-Functional Requirements)

## NFR01: Hiệu năng
- Hệ thống phải phản hồi thao tác người dùng trong thời gian không vượt quá 100ms.
- Đảm bảo tốc độ cập nhật trạng thái ổn định, không gây giật hoặc trễ.

## NFR02: Độ tin cậy
- Hệ thống hoạt động ổn định trong suốt thời gian sử dụng.
- Các sự kiện trong trò chơi được xử lý chính xác và nhất quán.

## NFR03: Khả dụng
- Giao diện người dùng rõ ràng, dễ quan sát và dễ sử dụng.
- Người dùng mới có thể nhanh chóng hiểu cách chơi mà không cần hướng dẫn.

## NFR04: Khả năng bảo trì
- Hệ thống được tổ chức để dễ dàng sửa đổi và nâng cấp.
- Thay đổi một thành phần không ảnh hưởng đến toàn bộ hệ thống.

## NFR05: Khả năng mở rộng
- Cho phép bổ sung tính năng mới (chế độ chơi, mức độ khó mới) mà không ảnh hưởng chức năng hiện tại.

## NFR06: Lưu trữ dữ liệu
- Hệ thống đảm bảo dữ liệu điểm cao được lưu trữ ổn định.
- Dữ liệu điểm cao không bị mất khi người dùng thoát hoặc tắt ứng dụng.

---

# 9. Yêu cầu miền (Domain Requirements & Business Rules)

- Bàn chơi được chia thành các ô theo dạng lưới.
- Rắn di chuyển theo từng bước giữa các ô liền kề.
- Rắn không được phép vượt qua giới hạn bàn chơi.
- Rắn không được phép di chuyển ngược hướng hiện tại ngay lập tức.
- Khi đầu rắn va chạm với biên hoặc thân rắn, trò chơi kết thúc theo quy tắc đã định.
- Thức ăn chỉ xuất hiện tại các vị trí không bị rắn chiếm.
- Mỗi lần rắn ăn thức ăn, chiều dài của rắn tăng thêm một đơn vị.
- Điểm số tăng tương ứng với số lượng thức ăn mà rắn đã ăn.
- Mức độ khó ảnh hưởng trực tiếp đến tốc độ cập nhật trạng thái trò chơi.

# 10. Giả định và ràng buộc (Assumptions & Constraints)

## 10.1 Giả định (Assumptions)

- Người dùng sử dụng thiết bị có hỗ trợ bàn phím (keyboard) để điều khiển.
- Môi trường chạy ứng dụng hỗ trợ hiển thị giao diện đồ họa (graphical user interface).
- Ứng dụng được triển khai trên môi trường máy tính để bàn (desktop environment) và hỗ trợ thao tác điều khiển bằng bàn phím (keyboard input).

---

## 10.2 Ràng buộc (Constraints)

- Hệ thống hoạt động độc lập, không yêu cầu kết nối mạng (network connection).
- Chỉ hỗ trợ chế độ một người chơi (single-player mode).
- Không tích hợp với các hệ thống hoặc dịch vụ bên ngoài (external systems/services).

---

# 11. Rủi ro (Risks)

- Lỗi trong việc phát hiện va chạm (collision detection) nếu logic xử lý không chính xác.
- Hiệu năng giảm (performance degradation) nếu việc cập nhật trạng thái không được tối ưu.
- Dữ liệu điểm cao (high score data) có thể bị mất nếu cơ chế lưu trữ cục bộ (local storage) gặp sự cố.
- Lỗi xử lý đầu vào (input handling error) nếu hệ thống không chặn đúng trường hợp rắn quay ngược 180°.

---

# 12. Tiêu chí thành công (Success Criteria)

- Hệ thống hoạt động ổn định (stable operation) trong quá trình chơi.
- Các chức năng chính hoạt động đúng theo yêu cầu đã xác định.
- Người dùng có thể hoàn thành một phiên chơi mà không xảy ra lỗi hệ thống.
- Điểm số và điểm cao được cập nhật chính xác và nhất quán.
---

# 13. Kết luận

Hệ thống Snake Game được xây dựng nhằm cung cấp trải nghiệm giải trí ổn định và mượt mà. Các yêu cầu nghiệp vụ đã xác định rõ các chức năng cốt lõi bao gồm điều khiển rắn, cập nhật trạng thái, xử lý sự kiện, quản lý điểm cao và tùy chọn độ khó. Với phạm vi tập trung vào chế độ chơi đơn và hoạt động độc lập, hệ thống đảm bảo tính khả thi và bền vững, tạo nền tảng cho các phiên bản nâng cao trong tương lai.
