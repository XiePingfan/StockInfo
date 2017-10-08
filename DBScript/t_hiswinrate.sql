INSERT INTO T_HisWinRate(StockID,StockName,StockBelong,EventMsg,WinRate) 
SELECT tall.StockID,tall.StockName,tall.StockBelong,tall.EventName,twin.win/tall.al*100 wrate
FROM
(SELECT t1.StockID,t2.StockName,t1.StockBelong,t1.EventName,COUNT(*) al
  FROM t_stock_event_sina t1,T_StockBaseInfo t2
WHERE t1.StockID = t2.StockID
  AND t1.StockBelong = t2.`StockBelong`
  AND t1.StockID = #{stockId}
GROUP BY StockID,StockBelong,EventName) tall,
(SELECT t1.StockID,t2.StockName,t1.StockBelong,t1.EventName,COUNT(*) win
  FROM t_stock_event_sina t1,T_StockBaseInfo t2
WHERE t1.StockID = t2.StockID
  AND t1.StockBelong = t2.StockBelong
  AND t1.IsWin = 'Y'
  AND t1.StockID = #{stockId}
GROUP BY StockID,StockBelong,EventName) twin
WHERE tall.StockID = twin.StockID
  AND tall.StockBelong = twin.StockBelong
  AND tall.EventName = twin.EventName