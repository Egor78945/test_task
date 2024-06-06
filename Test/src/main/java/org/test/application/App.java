package org.test.application;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class App {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Ручной ввод пути до jar-файла после запуска приложения
        File filePath = new File(sc.nextLine());


        Tickets tickets = null;

        try {
            // Преобразование json в объект
            tickets = objectMapper.readValue(filePath, Tickets.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Получение самых коротких полётов для каждой компании
        Map<String, Long> map = getMinCompaniesTime(tickets.getTickets());

        // Удобный и понятный вывод в консоль
        for (Map.Entry<String, Long> e : map.entrySet()) {
            if (TimeUnit.MILLISECONDS.toHours(e.getValue()) > 30) {
                map.put(e.getKey(), 0L);
            }
            System.out.println(String.format("%s : %s h, %s m, %s s",
                    e.getKey(),
                    TimeUnit.MILLISECONDS.toHours(e.getValue()),
                    TimeUnit.MILLISECONDS.toMinutes(e.getValue() / 10),
                    TimeUnit.MILLISECONDS.toSeconds(e.getValue() / 1000)));
        }

        List<Integer> prices = new ArrayList<>();

        // Получение всех цен за билеты
        Arrays.stream(tickets.getTickets()).forEach(e -> prices.add(e.getPrice()));

        // Вывод разности среднего значения и медианы
        System.out.println(String.format("Difference between average price and median price : %s", getAveragePrice(prices) - getMedianPrice(prices)));
    }

    private static Map<String, Long> getMinCompaniesTime(TicketInfo[] ticketInfos) {

        // Имя компании | минимальное время прилёта в миллисекундах
        Map<String, Long> companiesTime = new HashMap<>();

        for (TicketInfo t : ticketInfos) {
            // Получение даты прилёта из t
            Date date = new Date(t.getArrival_date().replace('.', '/').concat(" ").concat(t.getArrival_time()));

            // Проверка, если мапа содержит имя компании t, то при условии, если время прилёта в мапе больше времени прилёта t, обновляем значение в мапе
            if (!companiesTime.containsKey(t.getCarrier())) {
                companiesTime.put(t.getCarrier(), date.getTime());
            } else if (companiesTime.get(t.getCarrier()) > date.getTime()) {
                companiesTime.put(t.getCarrier(), companiesTime.get(t.getCarrier()) - date.getTime());
            }
        }

        return companiesTime;
    }

    private static int getMedianPrice(List<Integer> prices) {
        // Сортировка всех цен
        prices = prices.stream().sorted().collect(Collectors.toList());

        if (prices.size() % 2 == 0) {
            return (prices.get(prices.size() / 2) + prices.get(prices.size() / 2 - 1)) / 2;
        } else {
            return prices.get((prices.size() - 1) / 2);
        }
    }

    private static int getAveragePrice(List<Integer> prices) {
        return prices.stream().reduce((ac, i) -> ac += i).map(i -> i / prices.size()).get();
    }
}
