package org.test.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.test.application.models.TicketInfo;
import org.test.application.models.Tickets;

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
            System.out.println(String.format("%s : %s h, %s m, %s s",
                    e.getKey(),
                    e.getValue() / 1000 / 3600,
                    e.getValue() / 1000 / 60 % 60,
                    e.getValue() / 1000 % 60));
        }

        List<Integer> prices = new ArrayList<>();

        // Получение всех цен за билеты
        Arrays.stream(tickets.getTickets()).forEach(e -> prices.add(e.getPrice()));

        // Вывод разности среднего значения и медианы
        Map<String, Integer> differences = getAvgMedDifference(tickets.getTickets());
        for (Map.Entry<String, Integer> m : differences.entrySet()) {
            System.out.println(String.format("Difference between average and median of prices of %s : %s", m.getKey(), m.getValue()));
        }
    }

    private static Map<String, Long> getMinCompaniesTime(TicketInfo[] ticketInfos) {

        // Имя компании | минимальное время полёта в миллисекундах
        Map<String, Long> companiesTime = new HashMap<>();

        for (TicketInfo t : ticketInfos) {
            // Получение даты вылета из Владивостока
            Date departure_date = new Date(t.getDeparture_date().replace('.', '/').concat(" ").concat(t.getDeparture_time()));

            // Получение даты прилёта в Тель-Авив
            Date arrival_date = new Date(t.getArrival_date().replace('.', '/').concat(" ").concat(t.getArrival_time()));

            // Время полёта
            long flight_time = arrival_date.getTime() - departure_date.getTime();

            // Проверка, если имеющееся время полёта в мапе больше, чем текущее, то обновляем лежащее в мапе
            if (!companiesTime.containsKey(t.getCarrier())) {
                companiesTime.put(t.getCarrier(), flight_time);
            } else if (companiesTime.get(t.getCarrier()) > flight_time) {
                companiesTime.put(t.getCarrier(), flight_time);
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
            return prices.get(prices.size() / 2);
        }
    }

    private static int getAveragePrice(List<Integer> prices) {
        return prices.stream().reduce((ac, i) -> ac += i).map(i -> i / prices.size()).get();
    }

    private static Map<String, Integer> getAvgMedDifference(TicketInfo[] ticketInfos) {
        Map<String, List<Integer>> map = new HashMap<>();
        for (TicketInfo t : ticketInfos) {
            List<Integer> list;
            if (!map.containsKey(t.getCarrier())) {
                list = new ArrayList<>();
            } else {
                list = map.get(t.getCarrier());
            }
            list.add(t.getPrice());
            map.put(t.getCarrier(), list);
        }
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, List<Integer>> m : map.entrySet()) {
            result.put(m.getKey(), getAveragePrice(m.getValue()) - getMedianPrice(m.getValue()));
        }
        return result;
    }
}
