package com.eme22.bolo.entities;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;

@JsonAdapter(Poll.PollAdapter.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Poll {

    private String question;
    private ArrayList<Answers> answers = new ArrayList<>();

    public void addAnswer(Answers answer) {
        this.answers.add(answer);
    }

    public int getAnswerCount(int i) {
        return this.answers.get(i).getCount();
    }

    public void addVoteToAnswer(int answer, Long user){
        this.answers.get(answer).setCount(this.answers.get(answer).getCount()+1);
        this.answers.get(answer).addParticipant(user);
    }

    public void removeVoteFromAnswer(int answer, Long user){
        this.answers.get(answer).setCount(this.answers.get(answer).getCount()-1);
        this.answers.get(answer).removeParticipant(user);
    }

    public boolean isUserParticipant(int answer, Long user){
        return this.answers.get(answer).getParticipants().contains(user);
    }

    public boolean hasUserParticipated(Long user){
        for (Answers answer : answers) {
            if (answer.getParticipants().contains(user))
                return true;
        }
        return false;
    }

    public static class PollAdapter extends TypeAdapter<Poll> {

        @Override
        public void write(JsonWriter writer, Poll poll) throws IOException {
            writer.beginObject();
            writer.name("poll_question").value(poll.question);

            //writer.beginObject();
            writer.name("poll_answers");
            writer.beginArray();
            poll.answers.forEach( answers1 -> {
                try {
                    writer.beginObject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.name("poll_answer").value(answers1.answer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.name("poll_answer_count").value(answers1.count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.name("poll_participants");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.beginArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                answers1.participants.forEach( user -> {
                    try {
                        writer.value(user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                try {
                    writer.endArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.endObject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.endArray();
            writer.endObject();

        }

        @Override
        public Poll read(JsonReader reader) throws IOException {

            String question = null;
            ArrayList<Answers> answers = new ArrayList<>();
            reader.beginObject();
            while (reader.hasNext()){
                String name = reader.nextName();
                if (name.equals("poll_question")) {
                    question = String.valueOf(reader.nextString());
                }
                if (name.equals("poll_answers")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            Answers answers1 = new Answers();
                            String name2 = reader.nextName();
                            if (name2.equals("poll_answer")) {
                                answers1.setAnswer(reader.nextString());
                            }
                            if (name2.equals("poll_answer_count")) {
                                answers1.setCount(reader.nextInt());
                            }
                            if (name2.equals("poll_participants")){
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    answers1.addParticipant(reader.nextLong());
                                }
                                reader.endArray();
                            }
                            answers.add(answers1);
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
            return new Poll(question, answers);
        }
    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Answers {

        private String answer;
        private int count = 0;
        private ArrayList<Long> participants = new ArrayList<>();

        public void addParticipant(Long participant) {
            this.participants.add(participant);
        }

        public void removeParticipant(Long participant) {
            this.participants.remove(participant);
        }
    }
}
