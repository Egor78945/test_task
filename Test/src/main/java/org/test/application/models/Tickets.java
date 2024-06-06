package org.test.application.models;

public class Tickets {
    private TicketInfo[] tickets;

    public Tickets(TicketInfo[] tickets) {
        this.tickets = tickets;
    }

    public Tickets() {
    }

    public TicketInfo[] getTickets() {
        return tickets;
    }

    public void setTickets(TicketInfo[] tickets) {
        this.tickets = tickets;
    }
}
