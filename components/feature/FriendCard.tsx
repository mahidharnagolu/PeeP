import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Eye } from 'lucide-react-native';

interface FriendCardProps {
    name: string;
    status?: string;
    onPeep: () => void;
    peepsRemaining: number;
    isPeeping?: boolean;
}

export default function FriendCard({ name, status, onPeep, peepsRemaining, isPeeping }: FriendCardProps) {
    return (
        <View style={styles.card}>
            {/* Avatar circle with initial */}
            <View style={styles.avatar}>
                <Text style={styles.avatarText}>
                    {name.charAt(0).toUpperCase()}
                </Text>
            </View>

            <View style={styles.info}>
                <Text style={styles.name}>{name}</Text>
                <Text style={styles.status}>{status || 'Offline'}</Text>
            </View>

            <TouchableOpacity
                onPress={onPeep}
                style={[styles.peepButton, peepsRemaining <= 0 && styles.peepButtonDisabled]}
                disabled={peepsRemaining <= 0}
                activeOpacity={0.7}
            >
                <Eye color="#000000" size={16} />
                <Text style={[styles.peepText, peepsRemaining <= 0 && styles.disabledText]}>
                    {peepsRemaining > 0 ? 'Peep' : 'Limit'}
                </Text>
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    card: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#1A1A1A',
        padding: 14,
        borderRadius: 14,
        marginBottom: 10,
    },
    avatar: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: '#2A2A2A',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    avatarText: {
        color: '#FFFFFF',
        fontSize: 18,
        fontWeight: '700',
    },
    info: {
        flex: 1,
    },
    name: {
        color: '#FFFFFF',
        fontSize: 16,
        fontWeight: '600',
    },
    status: {
        color: '#999999',
        fontSize: 13,
        marginTop: 2,
    },
    peepButton: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: '#FFFFFF',
        paddingVertical: 8,
        paddingHorizontal: 16,
        borderRadius: 20,
        gap: 6,
    },
    peepButtonDisabled: {
        backgroundColor: '#2A2A2A',
    },
    peepText: {
        color: '#000000',
        fontWeight: '700',
        fontSize: 13,
    },
    disabledText: {
        color: '#666666',
    },
});
