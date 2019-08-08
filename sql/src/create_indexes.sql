DROP INDEX user_list_index;
DROP INDEX usr_index;
DROP INDEX user_list_contains_index;
DROP INDEX chat_index;
DROP INDEX chat_list_index;
DROP INDEX message_index;

CREATE INDEX user_list_index
ON USER_LIST
(list_id);

CREATE INDEX usr_index
ON USR
(login);

CREATE INDEX user_list_contains_index
ON USER_LIST_CONTAINS
(list_id, list_member);

CREATE INDEX chat_index
ON CHAT
(chat_id);

CREATE INDEX chat_list_index
ON CHAT_LIST
(chat_id, member);

CREATE INDEX message_index
ON MESSAGE
(msg_id);

