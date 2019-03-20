private boolean isSameDay(Date date1, Date date2) {
    Calendar calendar1 = Calendar.getInstance();
    calendar1.setTime(date1);
    Calendar calendar2 = Calendar.getInstance();
    calendar2.setTime(date2);
    boolean sameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    boolean sameMonth = calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    boolean sameDay = calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    return (sameDay && sameMonth && sameYear);
}

