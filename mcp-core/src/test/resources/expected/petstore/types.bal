public type Pet record {
    int id;
    string name;
    string species?;
    int age?;
};

public type NewPet record {
    string name;
    string species?;
    int age?;
};
