# BUSINESS REQUIREMENTS DOCUMENT (BRD)

## Project: Snake Game Application

---

## 1. Giới thiệu đề tài Snake Game

Snake Game (Rắn săn mồi) là một trò chơi điện tử quen thuộc, trong đó người chơi điều khiển một đối tượng (con rắn) di chuyển trong không gian dạng lưới để thu thập các phần tử (thức ăn). Mỗi lần thu thập thành công, đối tượng sẽ dài ra và điểm số của người chơi cũng tăng lên.

Trong quá trình chơi, độ khó có thể tăng dần thông qua tốc độ di chuyển hoặc cách xuất hiện của thức ăn. Trò chơi sẽ kết thúc khi đối tượng va chạm với biên hoặc tự va chạm với chính nó.

Hệ thống được xây dựng nhằm mang lại trải nghiệm giải trí đơn giản, dễ tiếp cận nhưng vẫn đủ tính tương tác. Đồng thời, đây cũng là nền tảng để có thể phát triển thêm các tính năng nâng cao trong tương lai.

---

## 2. Lý do chọn đề tài

* Trò chơi có luật chơi đơn giản nhưng vẫn thể hiện được đầy đủ các thành phần của một hệ thống phần mềm
* Dễ dàng triển khai các chức năng quan trọng như:

  * Xử lý thao tác từ người dùng
  * Cập nhật trạng thái theo thời gian
  * Quản lý dữ liệu thay đổi liên tục
* Có thể mở rộng thêm nhiều tính năng như tăng độ khó hoặc chế độ chơi
* Phù hợp để xây dựng một hệ thống rõ ràng, dễ hiểu và dễ bảo trì

---

## 3. Mục tiêu hệ thống

### 3.1 Mục tiêu tổng quát

Xây dựng một hệ thống Snake Game hoạt động ổn định, phản hồi nhanh và mang lại trải nghiệm chơi mượt mà cho người dùng.

---

### 3.2 Mục tiêu cụ thể

* Hệ thống phản hồi nhanh khi người dùng điều khiển
* Trạng thái trò chơi được cập nhật liên tục và chính xác
* Dữ liệu như vị trí, điểm số, trạng thái được quản lý nhất quán
* Giao diện hiển thị đúng với trạng thái hiện tại của trò chơi
* Có khả năng lưu và hiển thị điểm cao

---

## 4. Stakeholders

* **Người chơi (End Users):** Sử dụng hệ thống để giải trí
* **Nhóm phát triển (Development Team):** Xây dựng và duy trì hệ thống

---

## 5. Phạm vi hệ thống

### 5.1 In-Scope

* Điều khiển đối tượng trong trò chơi
* Cập nhật trạng thái theo thời gian
* Quản lý vị trí của các thành phần trong game
* Xử lý các sự kiện như ăn thức ăn và va chạm
* Tính toán và hiển thị điểm số
* Lưu trữ điểm cao
* Hiển thị trạng thái trò chơi như đang chơi, tạm dừng, kết thúc

---

### 5.2 Out-of-Scope

* Chế độ nhiều người chơi
* Kết nối mạng hoặc hệ thống server
* Các chức năng thanh toán
* Tích hợp mạng xã hội

---

## 6. Yêu cầu người dùng (User Requirements)

* UR01: Người dùng có thể điều khiển hướng di chuyển của đối tượng
* UR02: Hệ thống phản hồi ngay khi người dùng thao tác
* UR03: Người dùng có thể xem điểm số trong quá trình chơi
* UR04: Người dùng được thông báo khi trò chơi kết thúc
* UR05: Người dùng có thể chơi lại sau khi kết thúc
* UR06: Người dùng có thể xem lại điểm cao nhất

---

## 7. Yêu cầu chức năng (Functional Requirements)

### FR01: Điều khiển

* Hệ thống cho phép người dùng thay đổi hướng di chuyển của đối tượng thông qua các phím điều hướng trên bàn phím
* Hệ thống không cho phép đối tượng đổi sang hướng ngược lại ngay lập tức nhằm tránh va chạm không hợp lệ

---

### FR02: Cập nhật trạng thái

* Hệ thống phải tự động cập nhật trạng thái trò chơi theo các khoảng thời gian liên tục trong suốt quá trình chơi
* Việc cập nhật bao gồm vị trí đối tượng, kiểm tra sự kiện và trạng thái hiện tại của trò chơi

---

### FR03: Di chuyển

* Hệ thống đảm bảo đối tượng di chuyển liên tục theo hướng hiện tại cho đến khi có thay đổi từ người dùng
* Hệ thống cập nhật chính xác vị trí của đối tượng sau mỗi lần di chuyển

---

### FR04: Sinh phần tử

* Hệ thống phải tạo thức ăn tại các vị trí hợp lệ trong không gian trò chơi
* Hệ thống đảm bảo thức ăn không xuất hiện trùng với vị trí hiện tại của đối tượng

---

### FR05: Phát hiện sự kiện

* Hệ thống phải phát hiện khi đối tượng thu thập thức ăn
* Hệ thống phải phát hiện khi đối tượng va chạm với biên của không gian trò chơi
* Hệ thống phải phát hiện khi đối tượng va chạm với chính nó

---

### FR06: Xử lý khi ăn

* Khi đối tượng thu thập thức ăn, hệ thống phải tăng kích thước của đối tượng
* Hệ thống phải cập nhật điểm số tương ứng với số lần thu thập
* Hệ thống phải tạo một phần tử mới sau khi phần tử hiện tại được thu thập

---

### FR07: Quản lý điểm

* Hệ thống phải tính toán và cập nhật điểm số trong suốt quá trình chơi
* Điểm số phải được hiển thị theo thời gian thực cho người dùng

---

### FR08: Lưu điểm cao

* Hệ thống phải lưu trữ điểm số cao nhất đạt được
* Hệ thống phải đảm bảo dữ liệu điểm cao được giữ lại giữa các lần sử dụng

---

### FR09: Hiển thị

* Hệ thống phải hiển thị đầy đủ các thành phần của trò chơi bao gồm đối tượng, thức ăn và điểm số
* Giao diện phải phản ánh chính xác trạng thái hiện tại của trò chơi tại mọi thời điểm

---

### FR10: Kết thúc và chơi lại

* Hệ thống phải kết thúc trò chơi khi xảy ra va chạm
* Hệ thống phải hiển thị trạng thái kết thúc và điểm số đạt được
* Hệ thống phải cho phép người dùng bắt đầu lại trò chơi từ trạng thái ban đầu

---

## 8. Yêu cầu phi chức năng (Non-Functional Requirements)

### NFR01: Hiệu năng

* Hệ thống phải phản hồi thao tác người dùng trong thời gian không vượt quá 100ms
* Hệ thống phải đảm bảo tốc độ cập nhật trạng thái ổn định, không gây giật hoặc trễ trong quá trình chơi

---

### NFR02: Độ tin cậy

* Hệ thống phải hoạt động ổn định trong suốt thời gian sử dụng
* Các sự kiện trong trò chơi phải được xử lý chính xác và nhất quán

---

### NFR03: Khả dụng

* Giao diện người dùng phải rõ ràng, dễ quan sát và dễ sử dụng
* Người dùng mới có thể nhanh chóng hiểu cách chơi mà không cần hướng dẫn phức tạp

---

### NFR04: Khả năng bảo trì

* Hệ thống phải được tổ chức theo cách cho phép dễ dàng sửa đổi và nâng cấp
* Việc thay đổi một thành phần không được ảnh hưởng đến toàn bộ hệ thống

---

### NFR05: Khả năng mở rộng

* Hệ thống phải cho phép bổ sung thêm các tính năng mới như chế độ chơi hoặc mức độ khó mà không ảnh hưởng đến các chức năng hiện tại

---

### NFR06: Lưu trữ dữ liệu

* Hệ thống phải đảm bảo dữ liệu điểm cao được lưu trữ an toàn
* Dữ liệu không bị mất khi người dùng thoát hoặc tắt ứng dụng

---

## 9. Yêu cầu miền (Domain Requirements)

* Không gian trò chơi được chia thành các ô theo dạng lưới
* Đối tượng chỉ di chuyển theo từng bước giữa các ô liền kề
* Đối tượng không được phép vượt qua giới hạn không gian trò chơi
* Khi xảy ra va chạm, trò chơi phải kết thúc theo quy tắc đã định
* Thức ăn chỉ được xuất hiện tại các vị trí không bị chiếm bởi đối tượng

---

## 10. Giả định và ràng buộc (Assumptions & Constraints)

### Assumptions

* Người dùng sử dụng thiết bị có hỗ trợ bàn phím để điều khiển
* Môi trường chạy ứng dụng hỗ trợ hiển thị giao diện đồ họa

---

### Constraints

* Hệ thống hoạt động độc lập, không yêu cầu kết nối mạng
* Chỉ hỗ trợ chế độ một người chơi
* Không tích hợp với các hệ thống hoặc dịch vụ bên ngoài

---

## 11. Rủi ro (Risks)

* Có thể xảy ra lỗi trong việc phát hiện va chạm nếu xử lý không chính xác
* Hiệu năng có thể giảm nếu việc cập nhật trạng thái không được tối ưu
* Dữ liệu điểm cao có thể bị mất nếu cơ chế lưu trữ không ổn định

---


## 12. Kết luận

Hệ thống Snake Game được xác định với mục tiêu cung cấp một trải nghiệm chơi đơn giản, dễ tiếp cận nhưng vẫn đảm bảo tính phản hồi nhanh và ổn định trong quá trình sử dụng. Các yêu cầu nghiệp vụ đã làm rõ những chức năng cốt lõi mà hệ thống cần đáp ứng, bao gồm điều khiển đối tượng, cập nhật trạng thái theo thời gian, xử lý sự kiện trong trò chơi và quản lý điểm số.

Bên cạnh đó, các yêu cầu phi chức năng cũng đặt ra những tiêu chí quan trọng về hiệu năng, độ tin cậy, khả năng sử dụng và khả năng mở rộng, nhằm đảm bảo hệ thống không chỉ hoạt động đúng mà còn mang lại trải nghiệm tốt cho người dùng trong thực tế.

Phạm vi hệ thống được xác định rõ ràng, tập trung vào chế độ chơi đơn và hoạt động độc lập, giúp đảm bảo tính khả thi trong quá trình phát triển. Đồng thời, các giả định, ràng buộc và rủi ro cũng đã được xem xét để hạn chế các vấn đề có thể phát sinh.

Tổng thể, các yêu cầu nghiệp vụ đã định hướng rõ những gì hệ thống cần thực hiện, tạo nền tảng vững chắc cho việc phát triển một ứng dụng Snake Game ổn định, dễ sử dụng và có khả năng mở rộng trong các giai đoạn tiếp theo.
