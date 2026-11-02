# CT240T Lab 4 — Bài tập 1

Hệ thống đặt vé xem phim gồm 50 ghế, xử lý 100 yêu cầu bằng ExecutorService. Yêu cầu JDK 17 trở lên, không cần Maven hay thư viện ngoài.

Chạy trong PowerShell tại thư mục này:

```powershell
.\run.ps1 unsafe  # Skeleton không đồng bộ, pool 10 luồng
.\run.ps1 race    # Tăng tranh chấp: 100 luồng cùng đặt ghế 1
.\run.ps1 safe    # Lời giải: 50 thành công, 50 thất bại, tổng 100
.\run.ps1 test    # Kiểm chứng 100 worker cùng xuất phát
```

Nếu máy chặn script, dùng `powershell -ExecutionPolicy Bypass -File .\run.ps1 safe` (chỉ áp dụng cho tiến trình chạy lệnh).

## Bước 1 — Quan sát race condition

`UnsafeTicketBookingSystem.java` giữ thao tác kiểm tra ghế, đợi 10 ms, đánh dấu ghế và tăng biến đếm không có khóa. Hai luồng có thể cùng đọc ghế trống rồi cùng trả về thành công. Phép tăng biến đếm cũng có thể mất cập nhật.

Chế độ `unsafe` dùng cách phân ghế `(i % 50) + 1` và pool 10 luồng như đề. Lỗi không nhất thiết xuất hiện mỗi lần: hai lượt đặt cùng ghế cách nhau 50 tác vụ nên có thể không chồng lấn. Chế độ `race` dùng 100 worker cùng chờ tín hiệu rồi đặt ghế 1, tăng khả năng quan sát lỗi. Số lỗi phụ thuộc lịch chạy, không khẳng định một số liệu cố định.

## Bước 2 — Khắc phục

`TicketBookingSystem.java` dùng `ReentrantLock` bảo vệ toàn bộ chuỗi kiểm tra và cập nhật ghế; `unlock()` luôn nằm trong `finally`. Hai biến thống kê dùng `AtomicInteger`, đọc qua `get()` và tăng qua `incrementAndGet()`. AtomicInteger chỉ bảo vệ biến đếm; khóa mới bảo vệ quyết định đặt ghế. Nếu bị ngắt trong độ trễ giả lập, khôi phục cờ ngắt và hủy tác vụ trước khi thay đổi ghế.

Ghế ngoài 1..50 bị từ chối trước khi xử lý, không tính vào thống kê như mã khung. ExecutorService được chờ kết thúc, kiểm tra timeout và thu kết quả Future để không bỏ qua lỗi tác vụ.

## Bước 3 — Kiểm chứng

Main dùng 10 worker cho 100 yêu cầu, mỗi ghế được yêu cầu đúng hai lần: kết quả bắt buộc 50 thành công + 50 thất bại = 100. Chương trình báo lỗi nếu thống kê sai.

Kiểm thử chạy 5 vòng, mỗi vòng gồm hai kịch bản với 100 worker cùng xuất phát qua CountDownLatch: đặt trải đều 50 ghế (50/50) và cùng đặt ghế 1 (1/99). Kiểm tra từng kết quả trả về để phát hiện đè ghế độc lập với biến đếm; kiểm tra thêm ghế 0 và 51. Cuối cùng phải xuất hiện `PASS`.

Phạm vi: Bài tập 1 cá nhân; không bao gồm Bài tập 2 nhóm, Producer-Consumer hay thao tác GitHub.
