package com.project.notificationService.service;

import com.project.notificationService.client.AuthClient;
import com.project.notificationService.client.ExpenseClient;
import com.project.notificationService.dto.ExpenseDto;
import com.project.notificationService.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final AuthClient authClient;
    private final EmailService emailService;
    private final ExpenseClient expenseClient;

    @Value("${expenseiq.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Scheduled(cron = "0 * * * * *", zone = "IST")
//    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendDailyIncomeExpenseRemainder")
    public void sendDailyIncomeExpenseRemainder() {
        log.info("Job started: sendDailyIncomeExpenseRemainder()");
        List<UserDto> users = authClient.getAllUsers();
        for (UserDto user : users) {
             if (user.getIsActive() != null && user.getIsActive()) { // only active users
                String body = "Hi " + user.getName() + ",<br><br>"
                        + "This is a friendly remainder to add your income and expenses for today in ExpenseIQ.<br><br>"
                        + "<a href='" + frontendUrl + "' style='display:inline-block;padding:10px 20px;background-color:#4CAF50;color:#fff;text-decoration:none;border-radius:5px;font-weight:bold;'>Go to ExpenseIQ</a>"
                        + "<br><br>Best regards,<br>ExpenseIQ Team";
                emailService.sendEmail(user.getEmail(), "Daily remainder: Add your income and expenses", body);
             }
        }
        log.info("Job completed: sendDailyIncomeExpenseRemainder()");
    }
    @Scheduled(cron = "0 * * * * *", zone = "IST")
//    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendDailyExpenseSummary")
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary()");
        List<UserDto> users = authClient.getAllUsers();
        for (UserDto user : users) {
            if (user.getIsActive() != null && user.getIsActive()) { // only active users
//                List<ExpenseDto> todaysExpenses = expenseClient.filterExpenses(String.valueOf(user.getId()), LocalDate.now(), LocalDate.now());
                List<ExpenseDto> todaysExpenses = expenseClient.filterExpenses(String.valueOf(user.getId()), LocalDate.now().toString(), LocalDate.now().toString());
                if (todaysExpenses != null && !todaysExpenses.isEmpty()) {
                    StringBuilder table = new StringBuilder();
                    table.append("<table style='border-collapse:collapse;width:100%;'>");
                    table.append("<tr style='background-color:#f2f2f2;'>")
                            .append("<th style='border:1px solid #ddd;padding:8px;'>S.No</th>")
                            .append("<th style='border:1px solid #ddd;padding:8px;'>Name</th>")
                            .append("<th style='border:1px solid #ddd;padding:8px;'>Amount</th>")
                            .append("<th style='border:1px solid #ddd;padding:8px;'>Category</th>")
                            .append("</tr>");

                    int i = 1;
                    for (ExpenseDto expense : todaysExpenses) {
                        table.append("<tr>")
                                .append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>")
                                .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>")
                                .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>")
                                .append("<td style='border:1px solid #ddd;padding:8px;'>")
                                .append(expense.getCategoryId() != null && expense.getCategory() != null ? expense.getCategory().toString() : "N/A") // simplified mapping to display
                                .append("</td>")
                                .append("</tr>");
                    }
                    table.append("</table>");

                    String body = "Hi " + user.getName() + ",<br/><br/>"
                            + "Here is a summary of your expenses for today:<br/><br/>"
                            + table
                            + "<br/><br/>Best regards,<br/>ExpenseIQ Team";

                    emailService.sendEmail(user.getEmail(), "Your daily Expense Summary ", body);
                }
            }
        }
        log.info("Job Completed: sendDailyExpenseSummary()");
    }

    public void fallbackSendDailyIncomeExpenseRemainder(Throwable t) {
        log.error("Scheduled job skipped (Daily Remainder): Downstream services unavailable - {}", t.getMessage());
    }

    public void fallbackSendDailyExpenseSummary(Throwable t) {
        log.error("Scheduled job skipped (Daily Summary): Downstream services unavailable - {}", t.getMessage());
    }
}
